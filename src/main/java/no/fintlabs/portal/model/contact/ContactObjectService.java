package no.fintlabs.portal.model.contact;

import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;


@Service
public class ContactObjectService {

    @Value("${fint.ldap.contact-base}")
    private String contactBase;

    public void setupContact(Contact contact) {
        contact.setDn(getContactDn(contact.getNin()));
    }

    public Name getContactBase() {
        return LdapNameBuilder.newInstance(contactBase)
                .build();
    }

    public String getContactDn(String nin) {
        return LdapNameBuilder.newInstance(getContactBase())
                .add(LdapConstants.CN, nin)
                .build().toString();
    }

}
