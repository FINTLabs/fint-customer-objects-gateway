package no.fintlabs.portal.model.access;

import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.util.List;

@Service
public class AccessPackageTemplateService {

    private final LdapService ldapService;
    private final AccessPackageTemplateObjectService accessPackageTemplateObjectService;

    public AccessPackageTemplateService(LdapService ldapService, AccessPackageTemplateObjectService accessPackageTemplateObjectService) {
        this.ldapService = ldapService;
        this.accessPackageTemplateObjectService = accessPackageTemplateObjectService;
    }

    public List<AccessPackage> getAccessPackageTemplates() {
        return ldapService.getAll(accessPackageTemplateObjectService.getAccessBase().toString(), AccessPackage.class);
    }

    public boolean addAccessPackageTemplate(AccessPackage template) {
        LdapName dn = LdapNameBuilder.newInstance(accessPackageTemplateObjectService.getAccessBase())
                .add(LdapConstants.OU, template.getName())
                .build();
        template.setDn(dn);
        ldapService.createEntry(template);

        template.setSelf(dn);
        return ldapService.updateEntry(template);
    }

    public boolean updateAccessPackageTemplate(AccessPackage template) {
        return ldapService.updateEntry(template);
    }

    public void removeAccessPackageTemplate(AccessPackage template) {
        ldapService.deleteEntry(template);
    }

    public AccessPackage getAccessPackageTemplate(String accessId) {
        return getAccessPackageTemplateByDn(accessPackageTemplateObjectService.getAccessDn(accessId));
    }

    private AccessPackage getAccessPackageTemplateByDn(String accessDn) {
        return ldapService.getEntry(accessDn, AccessPackage.class);
    }
}