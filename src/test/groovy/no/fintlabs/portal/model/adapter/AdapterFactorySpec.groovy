package no.fintlabs.portal.model.adapter

import no.fintlabs.portal.model.organisation.Organisation
import spock.lang.Specification

class AdapterFactorySpec extends Specification {
    private AdapterFactory adapterFactory
    private Organisation organisation

    void setup() {
        adapterFactory = new AdapterFactory("ou=comp,o=fint")
        organisation = new Organisation(primaryAssetId: "test.no", name: "test_no")
    }

    def "Get Adapter Base"() {
        when:
        def dn = adapterFactory.getAdapterBase("orgUuid")

        then:
        dn
        dn.toString().contains("orgUuid")
    }

    def "Get Adapter Dn"() {
        when:
        def dn = adapterFactory.getAdapterDn("adapterUuid", organisation)

        then:
        dn != null
        dn.contains("adapterUuid")
    }

    def "Setup Adapter"() {
        given:
        def adapter = new Adapter(name: "TestAdapter")

        when:
        adapterFactory.setupAdapter(adapter, new Organisation(name: "name"))

        then:
        adapter.dn != null
        adapter.name != null
    }
}
