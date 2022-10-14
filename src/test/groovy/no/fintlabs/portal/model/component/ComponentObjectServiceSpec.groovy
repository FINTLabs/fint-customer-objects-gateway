package no.fintlabs.portal.model.component

import no.fintlabs.portal.ldap.LdapService
import spock.lang.Specification

class ComponentObjectServiceSpec extends Specification {
    private componentObjectService
    private ldapServiceMock

    void setup() {
        ldapServiceMock = Mock(LdapService)
        componentObjectService = new ComponentObjectService(componentBase: "ou=comp,o=fint", ldapService: ldapServiceMock)
    }

    def "Setup Component"() {
        given:
        def component = new Component(name: "test")

        when:
        componentObjectService.setupComponent(component)

        then:
        component.dn != null
        component.name != null
        1 * ldapServiceMock.getEntryByUniqueName(_ as String, _ as String, _ as Class) >> null
    }
}
