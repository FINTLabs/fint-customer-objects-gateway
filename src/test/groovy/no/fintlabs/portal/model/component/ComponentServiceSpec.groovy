package no.fintlabs.portal.model.component

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.OrganisationService
import no.fintlabs.portal.testutils.ObjectFactory
import spock.lang.Specification

class ComponentServiceSpec extends Specification {
    private componentService
    private componentObjectService
    private ldapService
    private organisationService
    private assetService

    def setup() {
        def componentBase = "ou=comp,o=fint"
        ldapService = Mock(LdapService)
        organisationService = Mock(OrganisationService)
        assetService = Mock(AssetService)
        componentObjectService = new ComponentObjectService(ldapService: ldapService, componentBase: componentBase)
        componentService = new ComponentService(
                ldapService: ldapService,
                componentBase: componentBase,
                componentObjectService: componentObjectService,
                organisationService: organisationService,
                assetService: assetService,
        )
    }

    def "Create Component"() {
        given:
        def component = ObjectFactory.newComponent()

        when:
        def created = componentService.createComponent(component)

        then:
        component.dn != null
        component.name != null
        created == true
        1 * ldapService.createEntry(_ as Component) >> true
    }

    def "Update Component"() {
        when:
        def updated = componentService.updateComponent(ObjectFactory.newComponent())

        then:
        updated == true
        1 * ldapService.updateEntry(_ as Component) >> true
    }

    def "Get Components"() {
        when:
        def components = componentService.getComponents()

        then:
        components.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newComponent(), ObjectFactory.newComponent())
    }

    def "Get Component By UUID"() {
        when:
        def component = componentService.getComponentByName(UUID.randomUUID().toString())

        then:
        component != null
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newComponent()
    }

    def "Delete Component"() {
        when:
        componentService.deleteComponent(ObjectFactory.newComponent())

        then:
        1 * ldapService.deleteEntry(_ as Component)
    }

    def "Get Component DN By UUID"() {
        given:
        def uuid = UUID.randomUUID().toString()

        when:
        def dn1 = componentService.getComponentDnByName(uuid)
        def dn2 = componentService.getComponentDnByName(null)

        then:
        dn1 != null
        dn1 == String.format("ou=%s,%s", uuid, componentObjectService.getComponentBase())
        dn1.contains(uuid) == true
        dn2 == null

    }

    def "Add Client to Component"() {
        given:
        def client = ObjectFactory.newClientWithDn()
        def component = ObjectFactory.newComponent()

        client.setDn("name=c1")
        component.setDn("ou=comp1")

        when:
        componentService.linkClient(component, client)

        then:
        component.getClients().size() == 1
        1 * ldapService.updateEntry(_ as Component)
    }

    def "Remove Client from Component"() {
        given:
        def component = ObjectFactory.newComponent()
        def c1 = ObjectFactory.newClientWithDn()
        def c2 = ObjectFactory.newClientWithDn()

        c1.setDn("name=c1,o=fint")
        c2.setDn("name=c2,o=fint")
        component.addClient(c1.getDn())
        component.addClient(c2.getDn())

        when:
        componentService.unLinkClient(component, c1)

        then:
        component.getClients().size() == 1
        component.getClients().get(0) == "name=c2,o=fint"
        1 * ldapService.updateEntry(_ as Component)
    }

    def "Add Adapter to Component"() {
        given:
        def adapter = ObjectFactory.newAdapter()
        def component = ObjectFactory.newComponent()

        adapter.setDn("name=a1")
        component.setDn("ou=comp1")

        when:
        componentService.linkAdapter(component, adapter)

        then:
        component.getAdapters().size() == 1
        1 * ldapService.updateEntry(_ as Component)
    }

    def "Remove Adapter from Component"() {
        given:
        def component = ObjectFactory.newComponent()
        def a1 = ObjectFactory.newAdapter()
        def a2 = ObjectFactory.newAdapter()

        a1.setDn("name=a1,o=fint")
        a2.setDn("name=a2,o=fint")
        component.addAdapter(a1.getDn())
        component.addAdapter(a2.getDn())

        when:
        componentService.unLinkAdapter(component, a1)

        then:
        component.getAdapters().size() == 1
        component.getAdapters().get(0) == "name=a2,o=fint"
        1 * ldapService.updateEntry(_ as Component)
    }

    def "Get Active Assets for Component"() {
        given:
        def component = ObjectFactory.newComponent()
        def org = ObjectFactory.newOrganisation()
        def ass = ObjectFactory.newAsset()
        component.organisations = [ 'ou=testOrg,ou=org,o=fint' ]

        when:
        def assets = componentService.getActiveAssetsForComponent(component)
        println(assets)

        then:
        assets.size() == 1
        1 * organisationService.getOrganisationByDn('ou=testOrg,ou=org,o=fint') >> Optional.of(org)
        1 * assetService.getAssets(org) >> [ ass ]
    }

}
