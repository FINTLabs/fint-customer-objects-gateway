package no.fintlabs.portal.model.adapter

import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.utilities.SecretService
import spock.lang.Specification

class AdapterFactorySpec extends Specification {
    def adapterFactory

    def setup() {
        adapterFactory = new AdapterFactory(new SecretService(), "ou=comp,o=fint")
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
        def dn = adapterFactory.getAdapterDn("adapterUuid", "orgUuid")

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
