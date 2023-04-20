package no.fintlabs.portal.model.client;

import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.LdapConstants;
import no.fintlabs.portal.utilities.PasswordUtility;
import no.fintlabs.portal.utilities.SecretService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class ClientFactory {

    private final String organisationBase;

    private final SecretService secretService;

    public ClientFactory(SecretService secretService, @Value("${fint.ldap.organisation-base}") String organisationBase) {
        this.secretService = secretService;
        this.organisationBase = organisationBase;
    }

    public void setupClient(Client client, Organisation organisation) {
        client.setName(getClientFullName(client.getName(), organisation.getPrimaryAssetId()));
        client.setDn(
                LdapNameBuilder.newInstance(getClientBase(organisation.getName()))
                        .add(LdapConstants.CN, client.getName())
                        .build()
        );
        //client.setPassword(secretService.generateSecret());
    }

    public String getClientFullName(String clientSimpleName, String organisationprimaryAssetId) {
        return String.format("%s@client.%s", clientSimpleName, organisationprimaryAssetId);
    }

    public Name getClientBase(String orgUuid) {
        return LdapNameBuilder.newInstance(organisationBase)
                .add(LdapConstants.OU, orgUuid)
                .add(LdapConstants.OU, LdapConstants.CLIENT_CONTAINER_NAME)
                .build();
    }

    public String getClientDn(String clientName, Organisation organisation) {
        return LdapNameBuilder.newInstance(getClientBase(organisation.getName()))
                .add(LdapConstants.CN, getClientFullName(clientName, organisation.getPrimaryAssetId()))
                .build()
                .toString();
    }
}
