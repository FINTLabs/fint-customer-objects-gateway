package no.fintlabs.portal.model.client

import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.utilities.SecretService
import spock.lang.Specification

class ClientFactorySpec extends Specification {
    def clientFactory
    def organisation

    void setup() {
        clientFactory = new ClientFactory(new SecretService(), "ou=org,o=fint")
        organisation = new Organisation(primaryAssetId: "test.no", name: "test_no")
    }

    def "Setup Client"() {
        given:
        def client = new Client(name: "TestClient")

        when:
        clientFactory.setupClient(client, new Organisation(name: "orgName"))

        then:
        client.password != null
        client.dn.contains("orgName")
        client.name != null
    }

    def "Get Client Base"() {
        when:
        def dn = clientFactory.getClientBase("orgUuid")

        then:
        dn != null
        dn.toString().contains("orgUuid")
    }

    def "Get Client Dn"() {
        when:
        def dn = clientFactory.getClientDn("clientUuid", organisation)

        then:
        dn != null
        dn.contains("clientUuid")
        dn.contains(organisation.getName())
    }
}
