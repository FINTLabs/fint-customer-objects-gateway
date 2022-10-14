package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.asset.AssetService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.oauth.NamOAuthClientService;
import no.fintlabs.portal.oauth.OAuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClientService {

    @Autowired
    private ClientObjectService clientObjectService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private NamOAuthClientService namOAuthClientService;

    public boolean addClient(Client client, Organisation organisation) {
        clientObjectService.setupClient(client, organisation);

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

        return created;
    }

    public List<Client> getClients(String orgName) {

        return ldapService.getAll(clientObjectService.getClientBase(orgName).toString(), Client.class);
    }

    public String getClientSecret(Client client) {
        return namOAuthClientService.getOAuthClient(client.getClientId()).getClientSecret();
    }

    public Optional<Client> getClient(String clientUuid, String orgUuid) {
        return getClientByDn(clientObjectService.getClientDn(clientUuid, orgUuid));
    }

    public Optional<Client> getClientByDn(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Client.class));
    }

    public boolean updateClient(Client client) {
        return ldapService.updateEntry(client);
    }

    public void deleteClient(Client client) {
        if (StringUtils.hasText(client.getClientId())) {
            namOAuthClientService.removeOAuthClient(client.getClientId());
        }
        ldapService.deleteEntry(client);
    }

    public void resetClientPassword(Client client, String newPassword) {
        client.setSecret(newPassword);
        ldapService.updateEntry(client);
    }


}
