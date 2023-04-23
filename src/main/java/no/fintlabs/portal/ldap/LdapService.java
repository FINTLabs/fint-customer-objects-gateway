package no.fintlabs.portal.ldap;

import no.fintlabs.portal.utilities.LdapUniqueNameUtility;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.List;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }


    private SearchControls searchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);

        return searchControls;
    }

    public boolean createEntry(BasicLdapEntry basicLdapEntry) {
        if (!entryExists(basicLdapEntry.getDn())) {
            ldapTemplate.create(basicLdapEntry);
            return true;
        }
        return false;
    }

    public boolean updateEntry(BasicLdapEntry basicLdapEntry) {
        if (entryExists(basicLdapEntry.getDn())) {
            ldapTemplate.update(basicLdapEntry);
            return true;
        }
        return false;
    }

    public <T> T getEntryByUniqueName(String name, String base, Class<T> type) {
        if (name != null && base != null) {
            List<T> ldapEntries = ldapTemplate.find(
                    LdapNameBuilder.newInstance(base).build(),
                    new EqualsFilter(LdapUniqueNameUtility.getUniqueNameAttribute(type), name),
                    searchControls(), type);

            if (ldapEntries != null && ldapEntries.size() == 1) {
                return ldapEntries.get(0);
            }
        }
        return null;
    }

    public <T> String getStringDnByUniqueName(String name, String base, Class<T> type) {
        BasicLdapEntry entry = ((BasicLdapEntry) getEntryByUniqueName(name, base, type));

        if (entry != null) {
            return entry.getDn();
        }

        return null;
    }

    public boolean entryExists(String dn) {
        try {
            ldapTemplate.lookup(LdapNameBuilder.newInstance(dn).build());
            return true;
        } catch (org.springframework.ldap.NamingException e) {
            return false;
        }
    }

    public <T> List<T> getAll(String base, Class<T> type) {
        if (entryExists(base)) {
            return ldapTemplate.findAll(LdapNameBuilder.newInstance(base).build(), searchControls(), type);
        }
        return null;
    }

    public <T> T getEntry(String dn, Class<T> type) {
        try {
            return ldapTemplate.findByDn(LdapNameBuilder.newInstance(dn).build(), type);
        } catch (org.springframework.ldap.NamingException e) {
            return null;
        }
    }

    public void deleteEntry(BasicLdapEntry basicLdapEntry) {
        ldapTemplate.delete(basicLdapEntry);
    }

}
