package no.fintlabs.portal.model.client

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.oauth.OAuthClient
import no.fintlabs.portal.testutils.ObjectFactory
import no.fintlabs.portal.utilities.SecretService
import spock.lang.Specification

import javax.naming.Name
import java.security.KeyPair
import java.security.KeyPairGenerator

import static no.fintlabs.portal.testutils.ObjectFactory.newClient

class ClientServiceSpec extends Specification {

    private clientService
    private ldapService
    private clientFactory
    private oauthService
    private assetService
    private organisation
    private db

    def setup() {
        def organisationBase = "ou=org,o=fint"

        ldapService = Mock(LdapService)
        oauthService = Mock(NamOAuthClientService)
        assetService = Mock(AssetService)
        db = Mock(ClientDBRepository)
        clientFactory = new ClientFactory(organisationBase)
        clientService = new ClientService(
                clientFactory,
                ldapService,
                assetService,
                oauthService,
                new SecretService(), db
        )

        organisation = organisation = new Organisation(primaryAssetId: "test.no", name: "test_no")

    }

    def "Add Client"() {
        given:
        def client = newClient()

        when:
        def createdClient = clientService.addClient(client, new Organisation(name: "name", primaryAssetId: "test.no"))

        then:
        createdClient.isPresent()
        createdClient.get().dn != null
        createdClient.get().name != null
        1 * ldapService.createEntry(_ as Client) >> true
        1 * ldapService.getEntry(_ as String, Client.class) >> client
        1 * oauthService.addOAuthClient(_ as String) >> new OAuthClient(clientId: "id")
        1 * oauthService.getOAuthClient(_ as String) >> new OAuthClient(clientId: "id", clientSecret: "secret")
        1 * db.findById(_ as Name) >> Optional.empty()
        4 * db.save(_ as Client) >> client
    }

    def "Get Clients"() {
        when:
        def clients = clientService.getClients("orgName")

        then:
        clients.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(newClient(), newClient())
        //2 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Get Client"() {
        given:
        def c = newClient()

        when:
        def client = clientService.getClientByName(c.getName(), organisation)

        then:
        client.isPresent()
        1 * ldapService.getEntry(_ as String, _ as Class) >> c
        1 * db.findById(_ as Name) >> Optional.empty()
        1 * db.save(_ as Client) >> c


        //1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Get Client Secret"() {
        when:
        def secret = clientService.getClientSecret(newClient())

        then:
        secret
        1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()

    }


    def "Update Client"() {
        given:
        def client = newClient()
        client.setDn("cn=name")
        when:
        def updated = clientService.updateClient(client)

        then:
        updated.isPresent()
        1 * ldapService.updateEntry(_ as Client) >> true
        1 * ldapService.getEntry(_ as String, _ as Class) >> client
        1 * db.findById(_ as Name) >> Optional.empty()
        1 * db.save(_ as Client) >> client
    }

    def "Delete Client"() {
        given:
        def client = newClient()
        client.setDn("cn=name")

        when:
        clientService.deleteClient(client)

        then:
        1 * ldapService.deleteEntry(_ as Client)
        1 * db.findById(_ as Name) >> Optional.empty()

    }

    def "Reset Client Password"() {
        given:
        def client = newClient()
        client.setDn("cn=name")

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        KeyPair pair = generator.generateKeyPair()

        when:
        def password = clientService.resetClientPassword(client)

        then:
        password
        1 * ldapService.updateEntry(_ as ClientPassword) >> true
    }

}
