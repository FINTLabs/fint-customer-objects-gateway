package no.fintlabs.portal.model.access;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.List;

@ApiModel
@Data
@Entry(objectClasses = {"organizationalUnit", "top", "fintAccess"})
public final class AccessPackage implements BasicLdapEntry {
    @Id
    private Name dn;

    @Attribute(name = "fintSelf")
    private Name self;

    @ApiModelProperty(value = "Technical name of the access package.")
    @Attribute(name = "ou")
    private String name;

    @Attribute(name = "fintAccessCollection")
    private List<String> collection;

    @Attribute(name = "fintAccessRead")
    private List<String> read;

    @Attribute(name = "fintAccessModify")
    private List<String> modify;

    @Attribute(name = "fintAccessClients")
    private List<String> clients;

    @Attribute(name = "fintAccessComponents")
    private List<String> components;

    @Attribute(name = "description")
    private String description;

    public void removeClient(String clientDn) {
        clients.removeIf(adapter -> adapter.equalsIgnoreCase(clientDn));
    }

    public void addClient(String clientDn) {
        if (clients.stream().noneMatch(clientDn::equalsIgnoreCase)) {
            clients.add(clientDn);
        }
    }

    public void removeComponent(String componentDn) {
        components.removeIf(adapter -> adapter.equalsIgnoreCase(componentDn));
    }

    public void addComponent(String clientDn) {
        if (components.stream().noneMatch(clientDn::equalsIgnoreCase)) {
            components.add(clientDn);
        }
    }

    public String getSelf() {
        if (self != null) {
            return self.toString();
        } else {
            return null;
        }
    }

    public void setSelf(Name self) {
        this.self = self;
    }

    public void setSelf(String self) {
        setSelf(LdapNameBuilder.newInstance(self).build());
    }

    @Override
    public String getDn() {
        if (dn != null) {
            return dn.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setDn(Name dn) {
        this.dn = dn;
    }

    @Override
    public void setDn(String dn) {
        setDn(LdapNameBuilder.newInstance(dn).build());
    }

}
