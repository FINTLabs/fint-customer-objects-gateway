package no.fintlabs.portal.utilities;

import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.contact.Contact;
import no.fintlabs.portal.model.organisation.Organisation;

public enum LdapUniqueNameUtility {
    ;

    public static <T> String getUniqueNameAttribute(Class<T> type) {

        if (type.getName().equals(Organisation.class.getName())) {
            return "fintOrgId";
        }

        if (type.getName().equals(Component.class.getName())) {
            return "fintCompTechnicalName";
        }

        if (type.getName().equals(Contact.class.getName())) {
            return "cn";
        }
        return null;
    }
}
