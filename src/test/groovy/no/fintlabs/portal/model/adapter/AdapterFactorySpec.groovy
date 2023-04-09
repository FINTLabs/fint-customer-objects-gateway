package no.fintlabs.portal.model.adapter

import no.fintlabs.portal.model.organisation.Organisation
import spock.lang.Specification

class AdapterFactorySpec extends Specification {
    def adapterObjectService

    def setup() {
        adapterObjectService = new AdapterObjectService(organisationBase: "ou=comp,o=fint")
    }

    def "Get Adapter Base"() {
        when:
        def dn = adapterObjectService.getAdapterBase("orgUuid")

        then:
        dn
        dn.toString().contains("orgUuid")
    }

    def "Get Adapter Dn"() {
        when:
        def dn = adapterObjectService.getAdapterDn("adapterUuid", "orgUuid")

        then:
        dn != null
        dn.contains("adapterUuid")
    }

    def "Setup Adapter"() {
        given:
        def adapter = new Adapter(name: "TestAdapter")

        when:
        adapterObjectService.setupAdapter(adapter, new Organisation(name: "name"))

        then:
        adapter.dn != null
        adapter.name != null
    }
}
