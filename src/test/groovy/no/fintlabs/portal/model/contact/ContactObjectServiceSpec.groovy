package no.fintlabs.portal.model.contact


import spock.lang.Specification

class ContactObjectServiceSpec extends Specification {

    def contactObjectService

    void setup() {
        def contactBase = "ou=contacts,o=fint"
        contactObjectService = new ContactObjectService(contactBase: contactBase)
    }

    def "Set Contact Dn"() {
        given:
        def contact = new Contact(nin: "12345")

        when:
        contactObjectService.setupContact(contact)

        then:
        contact.dn.contains(contact.nin)
    }

    def "Get Contact Dn"() {
        when:
        def dn = contactObjectService.getContactDn("nin")

        then:
        dn != null
        dn.contains("nin")
    }
}
