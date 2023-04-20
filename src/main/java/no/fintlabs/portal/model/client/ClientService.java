package no.fintlabs.portal.model.client;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ClientService
        implements FintCustomerObjectWithSecretsService<Client> {

    private final ClientFactory clientFactory;

    private final LdapService ldapService;

    private final AssetService assetService;

    private final NamOAuthClientService namOAuthClientService;

    private final SecretService secretService;

    private final ClientDBRepository db;


    public ClientService(ClientFactory clientFactory, LdapService ldapService, AssetService assetService,
                         NamOAuthClientService namOAuthClientService, SecretService secretService,
                         ClientDBRepository db) {
        this.clientFactory = clientFactory;
        this.ldapService = ldapService;
        this.assetService = assetService;
        this.namOAuthClientService = namOAuthClientService;
        this.secretService = secretService;
        this.db = db;
    }

    public Optional<Client> addClient(Client client, Organisation organisation) {
        clientFactory.setupClient(client, organisation);

        OAuthClient oAuthClient = namOAuthClientService.addOAuthClient(
                String.format("c_%s", client.getName()
                        .replace("@", "_")
                        .replace(".", "_")
                )
        );

        client.setClientId(oAuthClient.getClientId());

        boolean created = ldapService.createEntry(client);
        if (created) {
            Asset primaryAsset = assetService.getPrimaryAsset(organisation);
            assetService.linkClientToAsset(primaryAsset, client);
        }
        return getClientByDn(client.getDn())
                .map(createdClient -> {
                    createdClient.setPublicKey(client.getPublicKey());
                    resetAndEncryptPassword(createdClient, createdClient.getPublicKey());
                    encryptClientSecret(createdClient, createdClient.getPublicKey());
                    db.save(createdClient);

                    return createdClient;
                });

    }


    public List<Client> getClients(String orgName) {

        return ldapService.getAll(clientFactory.getClientBase(orgName).toString(), Client.class);
    }

    public String getClientSecret(Client client) {
        return namOAuthClientService.getOAuthClient(client.getClientId()).getClientSecret();
    }

    @Override
    public void encryptClientSecret(Client client, String publicKeyString) {
        client.setClientSecret(secretService.encryptPassword(
                namOAuthClientService.getOAuthClient(client.getClientId()).getClientSecret(),
                publicKeyString
        ));
        db.save(client);
    }

    @Override
    public void resetAndEncryptPassword(Client client, String privateKeyString) {
        client.setPassword(secretService.encryptPassword(resetClientPassword(client), privateKeyString));
        db.save(client);
    }

    public Optional<Client> getClientByName(String clientName, Organisation organisation) {
        return getClientByDn(clientFactory.getClientDn(clientName, organisation));
    }

    public Optional<Client> getClientByDn(String dn) {
        return db
                .findById(LdapNameBuilder.newInstance(dn).build())
                .map(client -> {
                    log.debug("Found client ({}) in database", client.getName());
                    return client;
                })
                .or(() -> {
                    log.debug("Check if client ({}) is in LDAP...", dn);
                    Optional<Client> client = Optional.ofNullable(ldapService.getEntry(dn, Client.class))
                            .map(db::save);
                    log.debug("Client exists: {}", client.isPresent());
                    return client;
                });
    }

    public Optional<Client> getClientByDnFromLdap(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Client.class));
    }

    public Optional<Client> updateClient(Client client) {
        if (ldapService.updateEntry(client)) {
            return getClientByDnFromLdap(client.getDn())
                    .map(updatedClient -> db.findById(LdapNameBuilder.newInstance(Objects.requireNonNull(updatedClient.getDn())).build())
                            .map(clientFromDb -> {
                                updatedClient.setClientSecret(clientFromDb.getClientSecret());
                                updatedClient.setPassword(clientFromDb.getPassword());
                                updatedClient.setPublicKey(clientFromDb.getPublicKey());
                                db.save(updatedClient);

                                return updatedClient;
                            })
                            .orElseGet(() -> db.save(updatedClient)));
        }
        return Optional.empty();
    }

    public Optional<Client> deleteClient(Client client) {
        if (StringUtils.hasText(client.getClientId())) {
            namOAuthClientService.removeOAuthClient(client.getClientId());
        }
        ldapService.deleteEntry(client);
        db.findById(LdapNameBuilder.newInstance(Objects.requireNonNull(client.getDn())).build())
                .map(Client::getName)
                .ifPresent(db::deleteByName);
        return Optional.of(client);
    }


    private String resetClientPassword(Client client) {
        String password = secretService.generateSecret();
        boolean updateEntry = ldapService.updateEntry(
                ClientPassword
                        .builder()
                        .dn(LdapNameBuilder.newInstance(Objects.requireNonNull(client.getDn())).build())
                        .password(password)
                        .build()
        );
        log.debug("Updating password is successfully: {}", updateEntry);

        return password;
    }


}
