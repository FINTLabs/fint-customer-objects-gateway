package no.fintlabs.portal.model.client

import no.fintlabs.portal.model.organisation.Organisation
import spock.lang.Specification

class ClientFactorySpec extends Specification {
    private ClientFactory clientFactory
    private Organisation organisation

    void setup() {
        clientFactory = new ClientFactory("ou=org,o=fint")
        organisation = new Organisation(primaryAssetId: "test.no", name: "test_no")
    }

    def "Setup Client"() {
        given:
        def client = new Client(name: "TestClient")

        when:
        clientFactory.setupClient(client, new Organisation(name: "orgName"))

        then:
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

    def "getClientFullName should return full client name for both simple name and full name"() {

        expect:
        clientFactory.getClientFullName(clientName, assetId) == clientFullName

        where:
        clientName            | assetId   || clientFullName
        "test"                | "test.no" || "test@client.test.no"
        "test@client.test.no" | "test.no" || "test@client.test.no"
    }
}
