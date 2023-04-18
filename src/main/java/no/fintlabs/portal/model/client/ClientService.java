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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClientService implements FintCustomerObjectWithSecretsService<Client> {

    private final ClientFactory clientFactory;

    private final LdapService ldapService;

    private final AssetService assetService;

    private final NamOAuthClientService namOAuthClientService;

    private final SecretService secretService;

    public ClientService(ClientFactory clientFactory, LdapService ldapService, AssetService assetService, NamOAuthClientService namOAuthClientService, SecretService secretService) {
        this.clientFactory = clientFactory;
        this.ldapService = ldapService;
        this.assetService = assetService;
        this.namOAuthClientService = namOAuthClientService;
        this.secretService = secretService;
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

        return getClientByDn(client.getDn());
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
    }

    @Override
    public void encryptPassword(Client client, String privateKeyString) {
        String password = secretService.generateSecret();
        client.setPassword(password);
        boolean updateEntry = ldapService.updateEntry(client);
        log.debug("Updating password is successfully: {}", updateEntry);
        client.setPassword(secretService.encryptPassword(password, privateKeyString));
    }

    public Optional<Client> getClientByName(String clientName, Organisation organisation) {
        return getClientByDn(clientFactory.getClientDn(clientName, organisation));
    }

    public Optional<Client> getClientByDn(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Client.class));
    }

    public Optional<Client> updateClient(Client client) {
        if (ldapService.updateEntry(client)) {
            return getClientByDn(client.getDn());
        }
        return Optional.empty();
    }

    public Optional<Client> deleteClient(Client client) {
        if (StringUtils.hasText(client.getClientId())) {
            namOAuthClientService.removeOAuthClient(client.getClientId());
        }
        ldapService.deleteEntry(client);
        return Optional.of(client);
    }

    @Deprecated
    public void resetClientPassword(Client client, String privateKeyString) {
        encryptPassword(client, privateKeyString);
    }


}
