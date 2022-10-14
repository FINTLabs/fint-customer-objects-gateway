package no.fintlabs.portal.model.access;

import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class AccessPackageTemplateObjectService {
    private final String accessTemplateBase;

    public AccessPackageTemplateObjectService(
            @Value("${fint.ldap.access.template-base}") String accessTemplateOrganisationBase
    ) {
        this.accessTemplateBase = accessTemplateOrganisationBase;
    }

    public Name getAccessBase() {
        return LdapNameBuilder.newInstance(accessTemplateBase)
                .build();
    }

    public String getAccessDn(String accessId) {
        return LdapNameBuilder.newInstance(getAccessBase())
                .add(LdapConstants.OU, accessId)
                .build()
                .toString();
    }
}

