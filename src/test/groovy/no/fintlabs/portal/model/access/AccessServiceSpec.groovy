package no.fintlabs.portal.model.access

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.client.Client
import no.fintlabs.portal.model.client.ClientObjectService
import no.fintlabs.portal.model.client.ClientService
import no.fintlabs.portal.oauth.NamOAuthClientService
import spock.lang.Specification

class AccessServiceSpec extends Specification {

    private clientService
    private ldapService
    private clientObjectService
    private oauthService
    private assetService
    private accessService
    private accessObjectService

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
        accessObjectService = new AccessObjectService(organisationBase)
        accessService = new AccessService(
                ldapService,
                accessObjectService,
                clientService
        )
    }

    def "Unlink old clients"() {
        when:
        accessService.unlinkOldClients(new AccessPackage(clients: ["a", "c"]), ["a", "b", "d"])

        then:
        ldapService.getEntry(_ as String, Client.class) >> new Client() >> new Client() >> new Client(accessPackages: ["q"])
        ldapService.getEntry(_ as String, AccessPackage.class) >> new AccessPackage(clients: Collections.singletonList("h"))
        2 * ldapService.updateEntry(_ as Client)
        1 * ldapService.updateEntry(_ as AccessPackage)
    }

    def "Link new clients"() {
        when:
        accessService.linkNewClients(new AccessPackage(clients: ["a", "c"]))

        then:
        ldapService.getEntry(_ as String, Client.class) >> new Client() >> new Client()
        2 * ldapService.updateEntry(_ as Client)
    }
}
