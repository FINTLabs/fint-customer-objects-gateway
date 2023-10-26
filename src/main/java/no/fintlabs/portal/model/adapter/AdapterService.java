package no.fintlabs.portal.model.adapter;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsService;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.asset.AssetService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.oauth.NamOAuthClientService;
import no.fintlabs.portal.oauth.OAuthClient;
import no.fintlabs.portal.utilities.SecretService;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class AdapterService implements FintCustomerObjectWithSecretsService<Adapter> {

    private final AdapterFactory adapterFactory;

    private final LdapService ldapService;

    private final NamOAuthClientService namOAuthClientService;

    private final AssetService assetService;

    private final SecretService secretService;

    private final AdapterDBRepository db;

    public AdapterService(AdapterFactory adapterFactory, LdapService ldapService, AssetService assetService,
                          NamOAuthClientService namOAuthClientService, SecretService secretService,
                          AdapterDBRepository db) {
        this.adapterFactory = adapterFactory;
        this.ldapService = ldapService;
        this.namOAuthClientService = namOAuthClientService;
        this.assetService = assetService;
        this.secretService = secretService;
        this.db = db;
    }

    public Optional<Adapter> addAdapter(Adapter adapter, Organisation organisation) {
        adapterFactory.setupAdapter(adapter, organisation);

        OAuthClient oAuthClient = namOAuthClientService.addOAuthClient(
                String.format("a_%s", adapter.getName()
                        .replace("@", "_")
                        .replace(".", "_")
                )
        );

        adapter.setClientId(oAuthClient.getClientId());

        boolean created = ldapService.createEntry(adapter);
        if (created) {
            Asset primaryAsset = assetService.getPrimaryAsset(organisation);
            assetService.linkAdapterToAsset(primaryAsset, adapter);
        }
        return getAdapterByDn(adapter.getDn())
                .map(createdAdapter -> {
                    createdAdapter.setPublicKey(adapter.getPublicKey());
                    resetAndEncryptPassword(createdAdapter, createdAdapter.getPublicKey());
                    encryptClientSecret(createdAdapter, createdAdapter.getPublicKey());
                    db.save(createdAdapter);

                    return createdAdapter;
                });
    }

    public List<Adapter> getAdapters(String orgName) {
        return ldapService.getAll(adapterFactory.getAdapterBase(orgName).toString(), Adapter.class);
    }

    public String getClientSecret(Adapter adapter) {
        return namOAuthClientService.getOAuthClient(adapter.getClientId()).getClientSecret();
    }

    @Override
    public void encryptClientSecret(Adapter adapter, String publicKeyString) {
        try {
            adapter.setClientSecret(secretService.encryptPassword(
                    namOAuthClientService.getOAuthClient(adapter.getClientId()).getClientSecret(),
                    publicKeyString
            ));
        } catch (Exception e) {
            log.error("Error when encrypting clientSecret" , e);
        }

        db.save(adapter);
    }

    @Override
    public void resetAndEncryptPassword(Adapter adapter, String privateKeyString) {
        try {
            adapter.setPassword(secretService.encryptPassword(resetAdapterPassword(adapter), privateKeyString));
        } catch (Exception e) {
            log.error("Error when encrypting password" , e);
        }

        db.save(adapter);
    }

    public Optional<Adapter> getAdapterByName(String adapterName, Organisation organisation) {
        return getAdapterByDn(adapterFactory.getAdapterDn(adapterName, organisation));
    }

    public Optional<Adapter> getAdapterByDn(String dn) {
        return db
                .findById(LdapNameBuilder.newInstance(dn).build())
                .map(adapter -> {
                    log.debug("Found adapter ({}) in database", adapter.getName());
                    return adapter;
                })
                .or(() -> {
                    log.debug("Check if adapter ({}) is in LDAP...", dn);
                    Optional<Adapter> adapter = Optional.ofNullable(ldapService.getEntry(dn, Adapter.class))
                            .map(db::save);
                    log.debug("Adapter exists: {}", adapter.isPresent());
                    return adapter;
                });
    }

    public Optional<Adapter> getAdapterByDnFromLdap(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Adapter.class));
    }

    public Optional<Adapter> updateAdapter(Adapter adapter) {

        if (!StringUtils.hasText(adapter.getPassword()) && StringUtils.hasText(adapter.getPublicKey())) {
            resetAndEncryptPassword(adapter, adapter.getPublicKey());
            log.warn("Get password because it's empty");
        }

        if (!StringUtils.hasText(adapter.getClientSecret()) && StringUtils.hasText(adapter.getPublicKey())) {
            encryptClientSecret(adapter, adapter.getPublicKey());
            log.warn("Get clientSecret from nam because it's empty");
        }

        if (ldapService.updateEntry(adapter)) {
            return getAdapterByDnFromLdap(adapter.getDn())
                    .map(updatedAdapter -> db.findById(LdapNameBuilder.newInstance(Objects.requireNonNull(updatedAdapter.getDn())).build())
                            .map(adapterFromDb -> {
                                updatedAdapter.setClientSecret(adapterFromDb.getClientSecret());
                                updatedAdapter.setPassword(adapterFromDb.getPassword());
                                updatedAdapter.setPublicKey(adapterFromDb.getPublicKey());
                                db.save(updatedAdapter);

                                return updatedAdapter;
                            })
                            .orElseGet(() -> db.save(updatedAdapter)));
        }
        return Optional.empty();
    }

    public Optional<Adapter> deleteAdapter(Adapter adapter) {
        if (StringUtils.hasText(adapter.getClientId())) {
            namOAuthClientService.removeOAuthClient(adapter.getClientId());
        }
        ldapService.deleteEntry(adapter);
        db.findById(LdapNameBuilder.newInstance(Objects.requireNonNull(adapter.getDn())).build())
                .map(Adapter::getName)
                .ifPresent(db::deleteByName);
        return Optional.of(adapter);
    }

    private String resetAdapterPassword(Adapter adapter) {
        String password = secretService.generateSecret();
        boolean updateEntry = ldapService.updateEntry(
                AdapterPassword
                        .builder()
                        .dn(LdapNameBuilder.newInstance(Objects.requireNonNull(adapter.getDn())).build())
                        .password(password)
                        .build()
        );
        log.debug("Updating password is successfully: {}", updateEntry);

        return password;
    }

}
