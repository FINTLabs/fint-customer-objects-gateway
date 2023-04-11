package no.fintlabs.portal.model.adapter;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.asset.AssetService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.oauth.NamOAuthClientService;
import no.fintlabs.portal.oauth.OAuthClient;
import no.fintlabs.portal.utilities.SecretService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdapterService {

    private final AdapterFactory adapterFactory;

    private final LdapService ldapService;

    private final NamOAuthClientService namOAuthClientService;

    private final AssetService assetService;

    private final SecretService secretService;

    public AdapterService(AdapterFactory adapterFactory, LdapService ldapService, NamOAuthClientService namOAuthClientService, AssetService assetService, SecretService secretService) {
        this.adapterFactory = adapterFactory;
        this.ldapService = ldapService;
        this.namOAuthClientService = namOAuthClientService;
        this.assetService = assetService;
        this.secretService = secretService;
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
        return getAdapter(adapter.getName(), organisation.getName());
    }

    public List<Adapter> getAdapters(String orgName) {
        return ldapService.getAll(adapterFactory.getAdapterBase(orgName).toString(), Adapter.class);
    }

    public Optional<Adapter> getAdapter(String adapterName, String orgName) {

        return getAdapterByDn(adapterFactory.getAdapterDn(adapterName, orgName));
    }

    public Optional<Adapter> getAdapterByDn(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Adapter.class));
    }

    public String getAdapterSecret(Adapter adapter) {
        return namOAuthClientService.getOAuthClient(adapter.getClientId()).getClientSecret();
    }

    public Optional<Adapter> updateAdapter(Adapter adapter) {
        if (ldapService.updateEntry(adapter)) {
            return getAdapterByDn(adapter.getDn());
        }
        return Optional.empty();
    }

    public Optional<Adapter> deleteAdapter(Adapter adapter) {
        if (StringUtils.hasText(adapter.getClientId())) {
            namOAuthClientService.removeOAuthClient(adapter.getClientId());
        }
        ldapService.deleteEntry(adapter);
        return Optional.of(adapter);
    }

    public void encryptClientSecret(Adapter adapter, String publicKeyString) {
        adapter.setClientSecret(secretService.encryptPassword(
                namOAuthClientService.getOAuthClient(adapter.getClientId()).getClientSecret(),
                publicKeyString
        ));
    }

    public void resetAdapterPassword(Adapter adapter, String privateKeyString) {
        String password = secretService.generateSecret();
        adapter.setPassword(password);
        boolean updateEntry = ldapService.updateEntry(adapter);
        log.debug("Updating password is successfully: {}", updateEntry);
        adapter.setPassword(secretService.encryptPassword(password, privateKeyString));
    }

}
