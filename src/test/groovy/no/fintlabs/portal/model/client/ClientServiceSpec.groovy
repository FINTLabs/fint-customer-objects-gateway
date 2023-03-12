package no.fintlabs.portal.model.client

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.oauth.OAuthClient
import no.fintlabs.portal.testutils.ObjectFactory
import spock.lang.Specification

class ClientServiceSpec extends Specification {

    private clientService
    private ldapService
    private clientObjectService
    private oauthService
    private assetService

    def setup() {
        def organisationBase = "ou=org,o=fint"

        ldapService = Mock(LdapService)
        oauthService = Mock(NamOAuthClientService)
        assetService = Mock(AssetService)
        clientObjectService = new ClientObjectService(organisationBase: organisationBase)
        clientService = new ClientService(
                clientObjectService: clientObjectService,
                ldapService: ldapService,
                namOAuthClientService: oauthService,
                assetService: assetService
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
        when:
        def updated = clientService.updateClient(ObjectFactory.newClient())

        then:
        updated == true
        1 * ldapService.updateEntry(_ as Client) >> true
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

        when:
        clientService.resetClientPassword(client, "FIXME")

        then:
        client.password != null
        1 * ldapService.updateEntry(_ as Client)
    }

}
