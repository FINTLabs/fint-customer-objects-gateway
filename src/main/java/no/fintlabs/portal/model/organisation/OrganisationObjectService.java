package no.fintlabs.portal.model.organisation;

import lombok.Getter;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class OrganisationObjectService {

    @Autowired
    LdapService ldapService;

    @Getter
    @Value("${fint.ldap.organisation-base}")
    private String organisationBase;

    public void setupOrganisation(Organisation organisation) {
        Organisation organisationFromLdap = ldapService.getEntryByUniqueName(organisation.getName(), organisationBase, Organisation.class);

        if (organisationFromLdap == null) {
            String name = organisation.getName().replace(".", "_");
            Name dn = LdapNameBuilder.newInstance(organisationBase)
                    .add(LdapConstants.OU, name)
                    .build();
            organisation.setDn(dn);
            organisation.setName(name);
        } else {
            organisation.setDn(LdapNameBuilder.newInstance(organisationFromLdap.getDn()).build());
        }
    }
}
