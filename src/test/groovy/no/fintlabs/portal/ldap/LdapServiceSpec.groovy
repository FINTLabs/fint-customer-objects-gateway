package no.fintlabs.portal.ldap

import no.fintlabs.portal.model.contact.Contact
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.filter.EqualsFilter
import spock.lang.Specification

import javax.naming.Name
import javax.naming.directory.SearchControls

class LdapServiceSpec extends Specification {

    private ldapService
    private ldapTemplate

    void setup() {
        ldapTemplate = Mock(LdapTemplate)
        ldapService = new LdapService(ldapTemplate: ldapTemplate)
    }

    def "Create Entry"() {
        def contact1 = new Contact(dn: "name=test1,o=fint.no", firstName: "Ole", lastName: "Olsen")
        def contact2 = new Contact(dn: "name=test2,o=fint.no", firstName: "Ola", lastName: "Hansen")

        when:
        def created1 = ldapService.createEntry(contact1)
        def created2 = ldapService.createEntry(contact2)

        then:
        created1 == true
        created2 == false
        ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null
        1 * ldapTemplate.create(_ as Contact)
    }

    def "Update Entry"() {
        def contact1 = new Contact(dn: "name=test1,o=fint.no", firstName: "Ole", lastName: "Olsen")
        def contact2 = new Contact(dn: "name=test2,o=fint.no", firstName: "Ola", lastName: "Hansen")

        when:
        def updated1 = ldapService.updateEntry(contact1)
        def updated2 = ldapService.updateEntry(contact2)

        then:
        updated1 == false
        updated2 == true
        ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null
        1 * ldapTemplate.update(_ as Contact)
    }

    def "Get Entry By Unique Name"() {
        when:
        def entry1 = ldapService.getEntryByUniqueName("test", "o=test", Contact.class)
        def entry2 = ldapService.getEntryByUniqueName(null, "test", Contact.class)
        def entry3 = ldapService.getEntryByUniqueName("test", "o=test", Contact.class)

        then:
        entry1 != null
        entry2 == null
        entry3 == null
        1 * ldapTemplate.find(_ as Name, _ as EqualsFilter, _ as SearchControls, _ as Class) >> Arrays.asList(new Contact())
        1 * ldapTemplate.find(_ as Name, _ as EqualsFilter, _ as SearchControls, _ as Class) >> Arrays.asList(new Contact(), new Contact())
    }

    def "Get String Dn By Unique Name"() {
        when:
        def dn = ldapService.getStringDnByUniqueName("test", "o=test", Contact.class)

        then:
        dn != null
        dn.contains("ou")
        1 * ldapTemplate.find(_ as Name, _ as EqualsFilter, _ as SearchControls, Contact.class) >> Arrays.asList(new Contact(dn: "ou=test"))
    }

    def "Entry Exists"() {
        when:
        def exists1 = ldapService.entryExists("o=test")
        def exists2 = ldapService.entryExists("o=test")

        then:
        exists1 == false
        exists2 == true
        2 * ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null
    }

    def "Get Entry"() {
        when:
        def entry1 = ldapService.getEntry("o=test1", Contact.class)
        def entry2 = ldapService.getEntry("o=test2", Contact.class)

        then:
        entry1 != null
        entry2 == null
        2 * ldapTemplate.findByDn(_ as Name, _ as Class) >> new Contact() >> { throw new NameNotFoundException("test") }
    }

    def "Get All"() {
        when:
        List<Contact> all = ldapService.getAll("o=fint", Contact.class)

        then:
        all.size() == 2
        1 * ldapTemplate.findAll(_ as Name, _ as SearchControls, _ as Class) >> Arrays.asList(new Contact(), new Contact())
    }

    def "Delete Entry"() {
        when:
        ldapService.deleteEntry(new Contact())

        then:
        1 * ldapTemplate.delete(_ as Contact)
    }
}
