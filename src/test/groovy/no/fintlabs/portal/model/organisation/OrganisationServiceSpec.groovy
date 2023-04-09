package no.fintlabs.portal.model.organisation

import no.fintlabs.portal.ldap.Container
import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.adapter.Adapter
import no.fintlabs.portal.model.adapter.AdapterObjectService
import no.fintlabs.portal.model.adapter.AdapterService
import no.fintlabs.portal.model.asset.Asset
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.client.Client
import no.fintlabs.portal.model.client.ClientFactory
import no.fintlabs.portal.model.client.ClientService
import no.fintlabs.portal.model.component.Component
import no.fintlabs.portal.model.component.ComponentObjectService
import no.fintlabs.portal.model.component.ComponentService
import no.fintlabs.portal.model.contact.Contact
import no.fintlabs.portal.model.contact.ContactService
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.testutils.ObjectFactory
import no.fintlabs.portal.utilities.SecretService
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.IntStream

class OrganisationServiceSpec extends Specification {
    private organisationService
    private ldapService
    private organisationObjectService
    private contactService
    private adapterService
    private clientService
    private oauthService
    private componentService
    private assetService

    def setup() {
        def organisationBase = "ou=org,o=fint"
        def componentBase = "ou=comp,o=fint"
        def clientObjectService = new ClientFactory(organisationBase: organisationBase)
        def adapterObjectService = new AdapterObjectService(organisationBase: organisationBase)

        ldapService = Mock(LdapService)
        oauthService = Mock(NamOAuthClientService)
        assetService = new AssetService(ldapService: ldapService)
        contactService = Mock(ContactService)
        adapterService = new AdapterService(
                adapterObjectService: adapterObjectService,
                ldapService: ldapService,
                namOAuthClientService: oauthService,
                assetService: assetService
        )
        clientService = new ClientService(
                clientObjectService,
                ldapService,
                assetService,
                oauthService,
                new SecretService()
        )
        organisationObjectService = new OrganisationObjectService(organisationBase: organisationBase, ldapService: ldapService)
        componentService = new ComponentService(
                componentBase: componentBase,
                ldapService: ldapService,
                componentObjectService: new ComponentObjectService(ldapService: ldapService),
        )
        organisationService = new OrganisationService(
                organisationBase: organisationBase,
                ldapService: ldapService,
                organisationObjectService: organisationObjectService,
                contactService: contactService,
                adapterService: adapterService,
                clientService: clientService,
                componentService: componentService,
                assetService: assetService
        )
    }

    def "Create Organisation"() {
        given:
        def organisation = ObjectFactory.newOrganisation()

        when:
        def createdOrganisation = organisationService.createOrganisation(organisation)

        then:
        createdOrganisation
        createdOrganisation.dn != null
        createdOrganisation.name != null
        1 * ldapService.createEntry(_ as Organisation) >> true
        1 * ldapService.getEntry(_ as String, Organisation.class) >> organisation
        1 * ldapService.getAll(_, Asset.class) >> [new Asset(name: "name", primaryAsset: true)]
    }

    def "Update Organisation"() {
        when:
        def updated = organisationService.updateOrganisation(ObjectFactory.newOrganisation())

        then:
        updated == true
        1 * ldapService.updateEntry(_ as Organisation) >> true
    }

    def "Get All Organisations"() {
        when:
        def organisations = organisationService.getOrganisations()

        then:
        organisations.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newOrganisation(), ObjectFactory.newOrganisation())
        2 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newAsset())
    }

    def "Get Organisation"() {
        when:
        def organisation = organisationService.getOrganisation("jalla")

        then:
        organisation.isPresent()
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newOrganisation()
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newAsset())

    }

    def "Delete Organisation"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        organisation.name = UUID.randomUUID().toString()
        organisation.dn = String.format("ou=%s,ou=org,o=fint", organisation.name)

        when:
        organisationService.deleteOrganisation(organisation)

        then:
        1 * ldapService.deleteEntry(_ as Organisation)
        2 * ldapService.deleteEntry(_ as Client)
        2 * ldapService.deleteEntry(_ as Adapter)
        3 * ldapService.deleteEntry(_ as Container)
        //4 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
        3 * ldapService.getAll(_ as String, _ as Class) >>
                Arrays.asList(ObjectFactory.newAdapter(), ObjectFactory.newAdapter()) >>
                Arrays.asList(ObjectFactory.newClient(), ObjectFactory.newClient()) >>
                Arrays.asList(ObjectFactory.newAsset(), ObjectFactory.newAsset())
    }


    def "Add component to organisation"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def component = ObjectFactory.newComponent()

        organisation.setDn("ou=org1")
        component.setDn("ou=comp1")

        when:
        organisationService.linkComponent(organisation, component)

        then:
        organisation.getComponents().size() == 1
        1 * ldapService.updateEntry(_ as Organisation)
        1 * ldapService.updateEntry(_ as Component)
    }

    def "Remove component from organisation"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def comp1 = ObjectFactory.newComponent()
        def comp2 = ObjectFactory.newComponent()

        comp1.setDn("ou=comp1,o=fint")
        comp2.setDn("ou=comp2,o=fint")
        organisation.addComponent("ou=comp1,o=fint")
        organisation.addComponent("ou=comp2,o=fint")

        when:
        organisationService.unLinkComponent(organisation, comp1)

        then:
        organisation.getComponents().size() == 1
        organisation.getComponents().get(0) == "ou=comp2,o=fint"
        1 * ldapService.getAll(_ as String, _ as Class<List<Client>>) >> Arrays.asList(ObjectFactory.newClient())
        1 * ldapService.getAll(_ as String, _ as Class<List<Adapter>>) >> Arrays.asList(ObjectFactory.newAdapter())
        1 * ldapService.updateEntry(_ as Organisation)
        3 * ldapService.updateEntry(_ as Component)


    }

    def "Link Legal Contact"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact1 = ObjectFactory.newContact("11111111111")
        def contact2 = ObjectFactory.newContact("22222222222")

        when:
        organisationService.linkLegalContact(organisation, contact1)

        then:
        organisation.legalContact
        contact1.legal.any { it == organisation.dn }
        1 * ldapService.updateEntry(_ as Organisation)
        1 * contactService.updateContact(_ as Contact)

        when:
        organisationService.linkLegalContact(organisation, contact2)

        then:
        organisation.legalContact == contact2.dn
        !contact1.legal.any { it == organisation.dn }
        contact2.legal.any { it == organisation.dn }
        1 * ldapService.updateEntry(_ as Organisation)
        2 * contactService.updateContact(_ as Contact)
        1 * contactService.getContactByDn(_) >> Optional.of(contact1)
    }

    def "Unlink Legal Contact"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact = ObjectFactory.newContact("11111111111")

        when:
        organisationService.linkLegalContact(organisation, contact)

        then:
        organisation.legalContact
        contact.legal.any { it == organisation.dn }

        when:
        organisationService.unLinkLegalContact(organisation, contact)

        then:
        organisation.legalContact == null
        contact.legal.isEmpty()
        1 * ldapService.updateEntry(_ as Organisation)
        1 * ldapService.updateEntry(_ as Contact)
    }

    def "Link Technical Contact"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact = ObjectFactory.newContact("11111111111")

        when:
        organisationService.linkTechnicalContact(organisation, contact)

        then:
        organisation.techicalContacts.any { it == contact.dn }
        contact.technical.any { it == organisation.dn }
        1 * ldapService.updateEntry(_ as Organisation)
        1 * ldapService.updateEntry(_ as Contact)
    }

    def "Unlink Technical Contact"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact1 = ObjectFactory.newContact("11111111111")
        def contact2 = ObjectFactory.newContact("11111111111")
        contact2.dn = "cn=22222222,ou=contacts,o=fint"

        when:
        organisationService.linkTechnicalContact(organisation, contact1)
        organisationService.linkTechnicalContact(organisation, contact2)

        then:
        organisation.techicalContacts.any { it == contact1.dn }
        contact1.technical.any { it == organisation.dn }
        organisation.techicalContacts.any { it == contact2.dn }
        contact2.technical.any { it == organisation.dn }

        when:
        organisationService.unLinkTechnicalContact(organisation, contact2)

        then:
        organisation.techicalContacts.any { it == contact1.dn }
        organisation.techicalContacts.every { it != contact2.dn }
        contact1.technical.any { it == organisation.dn }
        contact2.technical.isEmpty()
        1 * ldapService.updateEntry(_ as Organisation)
        1 * ldapService.updateEntry(_ as Contact)
    }

    def "Get Legal Contact"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        organisation.legalContact = "dn=11111111111,ou=contacts,o=fint"

        when:
        def contact = organisationService.getLegalContact(organisation)

        then:
        contact
        1 * contactService.getContacts() >> IntStream.rangeClosed(1, 9).mapToObj(Integer.&toString).map { def o = ObjectFactory.newContact("11111111111")
            o.nin = it * 11; o.dn = "dn=" + o.nin + ",ou=contacts,o=fint"; o }.collect(Collectors.toList())
    }

    def "Get Technical Contacts"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        organisation.techicalContacts = ["dn=33333333333,ou=contacts,o=fint", "dn=77777777777,ou=contacts,o=fint"]

        when:
        def contacts = organisationService.getTechnicalContacts(organisation)

        then:
        contacts.size() == 2
        1 * contactService.getContacts() >> IntStream.rangeClosed(1, 9).mapToObj(Integer.&toString).map { def o = ObjectFactory.newContact("11111111111")
            o.nin = it * 11; o.dn = "dn=" + o.nin + ",ou=contacts,o=fint"; o }.collect(Collectors.toList())
    }

    def "When adding admin role, all other roles should be removed"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact = ObjectFactory.newContact("11111111111")
        contact.setRoles(['ROLE_MONKEY@TestOrganisation', 'ROLE_STUPID@TestOrganisation'])

        when:
        organisationService.addRoles(organisation, contact, ["ROLE_ADMIN"])

        then:
        1 * contactService.updateContact(_ as Contact) >> true
        contact.getRoles().size() == 1
        contact.roles.every { it == 'ROLE_ADMIN@TestOrganisation' }

    }

    def "When adding admin role, no roles for other organisations should be removed"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact = ObjectFactory.newContact("11111111111")
        contact.setRoles(['ROLE_MONKEY@bar.org', 'ROLE_STUPID@foo.org', 'ROLE_COCKY@TestOrganisation'])

        when:
        organisationService.addRoles(organisation, contact, ["ROLE_ADMIN"])

        then:
        1 * contactService.updateContact(_ as Contact) >> true
        contact.getRoles().size() == 3
        contact.roles.any { it == 'ROLE_ADMIN@TestOrganisation' }

    }

    def "When adding non-admin role, admin role for organisation should be removed"() {
        given:
        def organisation = ObjectFactory.newOrganisation()
        def contact = ObjectFactory.newContact("11111111111")
        contact.setRoles(['ROLE_MONKEY@bar.org', 'ROLE_STUPID@foo.org', 'ROLE_ADMIN@TestOrganisation'])

        when:
        organisationService.addRoles(organisation, contact, ["ROLE_PESANT"])

        then:
        1 * contactService.updateContact(_ as Contact) >> true
        contact.getRoles().size() == 3
        !contact.roles.any { it == 'ROLE_ADMIN@TestOrganisation' }

    }

}
