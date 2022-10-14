package no.fintlabs.portal.model.contact;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ContactService {

    public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    private final LdapService ldapService;
    private final ContactObjectService contactObjectService;

    public ContactService(LdapService ldapService, ContactObjectService contactObjectService) {
        this.ldapService = ldapService;
        this.contactObjectService = contactObjectService;
    }

    public List<Contact> getContacts() {
        return ldapService.getAll(contactObjectService.getContactBase().toString(), Contact.class);
    }

    public boolean addContact(Contact contact) {
        log.info("Creating contact: {}", contact);

        contactObjectService.setupContact(contact);
        return ldapService.createEntry(contact);
    }

    public Optional<Contact> getContact(String nin) {
        return getContactByDn(contactObjectService.getContactDn(nin));
    }

    public Optional<Contact> getContactByDn(String dn) {

        return Optional.ofNullable(ldapService.getEntry(dn, Contact.class));
    }

    public boolean updateContact(Contact contact) {
        return ldapService.updateEntry(contact);
    }

    public void deleteContact(Contact contact) {
        ldapService.deleteEntry(contact);
    }

}
