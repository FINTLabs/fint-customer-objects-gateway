package no.fintlabs.portal.model.client;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;

//@AllArgsConstructor
@Jacksonized
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
@Entry(objectClasses = {"fintClient", "inetOrgPerson", "organizationalPerson", "person", "top"})
public class ClientPassword implements BasicLdapEntry {

    @Id
    private Name dn;

    @Getter
    @Setter
    @Attribute(name = "userPassword")
    private String password;

    @Override
    public String getDn() {
        if (dn != null) {
            return dn.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setDn(String dn) {
        this.dn = LdapNameBuilder.newInstance(dn).build();
    }

    @Override
    public void setDn(Name dn) {
        this.dn = dn;
    }
}

