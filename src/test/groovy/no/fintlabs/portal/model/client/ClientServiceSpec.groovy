package no.fintlabs.portal.model.client

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.oauth.OAuthClient
import no.fintlabs.portal.testutils.ObjectFactory
import no.fintlabs.portal.utilities.SecretService
import spock.lang.Specification

import java.security.KeyPair
import java.security.KeyPairGenerator

class ClientServiceSpec extends Specification {

    private clientService
    private ldapService
    private clientFactory
    private oauthService
    private assetService

    def setup() {
        def organisationBase = "ou=org,o=fint"

        ldapService = Mock(LdapService)
        oauthService = Mock(NamOAuthClientService)
        assetService = Mock(AssetService)
        clientFactory = new ClientFactory(new SecretService(), organisationBase)
        clientService = new ClientService(
                clientFactory,
                ldapService,
                assetService,
                oauthService,
                new SecretService()
        )
    }

    def "Add Client"() {
        given:
        def client = ObjectFactory.newClient()

        when:
        def createdClient = clientService.addClient(client, new Organisation(name: "name", primaryAssetId: "test.no"))

        then:
        createdClient.isPresent()
        createdClient.get().dn != null
        createdClient.get().name != null
        1 * ldapService.createEntry(_ as Client) >> true
        1 * ldapService.getEntry(_ as String, Client.class) >> client
        1 * oauthService.addOAuthClient(_ as String) >> new OAuthClient()
    }

    def "Get Clients"() {
        when:
        def clients = clientService.getClients("orgName")

        then:
        clients.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newClient(), ObjectFactory.newClient())
        //2 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Get Client"() {
        when:
        def client = clientService.getClient(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        then:
        client.isPresent()
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newClient()
        //1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Get Adapter OpenID Secret"() {
        when:
        def client = clientService.getClient(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        def secret = clientService.getClientSecret(client.get())

        then:
        secret
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newClient()
        1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }


    def "Update Client"() {
        given:
        def client = ObjectFactory.newClient()
        client.setDn("cn=name")
        when:
        def updated = clientService.updateClient(client)

        then:
        updated.isPresent()
        1 * ldapService.updateEntry(_ as Client) >> true
        1 * ldapService.getEntry(_ as String, _ as Class) >> client

    }

    def "Delete Client"() {
        when:
        clientService.deleteClient(ObjectFactory.newClient())

        then:
        1 * ldapService.deleteEntry(_ as Client)
    }

    def "Reset Client Password"() {
        given:
        def client = ObjectFactory.newClient()

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        KeyPair pair = generator.generateKeyPair()

        when:
        clientService.resetClientPassword(client, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()))

        then:
        1 * ldapService.updateEntry(_ as Client)
    }

}
