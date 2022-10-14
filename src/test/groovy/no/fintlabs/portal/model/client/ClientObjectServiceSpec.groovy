package no.fintlabs.portal.model.client

import no.fintlabs.portal.model.organisation.Organisation
import spock.lang.Specification

class ClientObjectServiceSpec extends Specification {
    def clientObjectService

    void setup() {
        clientObjectService = new ClientObjectService(organisationBase: "ou=org,o=fint")
    }

    def "Setup Client"() {
        given:
        def client = new Client(name: "TestClient")

        when:
        clientObjectService.setupClient(client, new Organisation(name: "orgName"))

        then:
        client.password != null
        client.dn.contains("orgName")
        client.name != null
    }

    def "Get Client Base"() {
        when:
        def dn = clientObjectService.getClientBase("orgUuid")

        then:
        dn != null
        dn.toString().contains("orgUuid")
    }

    def "Get Client Dn"() {
        when:
        def dn = clientObjectService.getClientDn("clientUuid", "orgUuid")

        then:
        dn != null
        dn.contains("clientUuid")
        dn.toString().contains("orgUuid")
    }
}
