package no.fintlabs

import no.fintlabs.portal.model.client.ClientEvent
import spock.lang.Specification

class FintCustomerObjectEventSpec extends Specification {

    def "Get organisation object name should replace the dot in the orgId with _"() {
        given:
        def event = ClientEvent.builder().orgId("test.no").build()

        when:
        def name = event.getOrganisationObjectName()

        then:
        name == "test_no"
    }

    def "If there is multiple dots in orgId all of them should be replaced"() {
        given:
        def event = ClientEvent.builder().orgId("test.test.test.no").build()

        when:
        def name = event.getOrganisationObjectName()

        then:
        name == "test_test_test_no"
    }
}
