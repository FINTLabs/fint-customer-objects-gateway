package no.fintlabs.portal.model.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;

@ApiModel
@Data
@Entry(objectClasses = {"inetOrgPerson", "organizationalPerson", "person", "top", "fintContact"})
public final class Contact implements BasicLdapEntry {

    @ApiModelProperty(value = "DN of the contact. This is automatically set.")
    @Id
    private Name dn;

    @ApiModelProperty(value = "National Idenitification Number (NIN). This would be fodselsnummer (11 digits)")
    @Attribute(name = "cn")
    private String nin;

    @ApiModelProperty(value = "First name of the contact.")
    @Attribute(name = "givenName")
    private String firstName;

    @ApiModelProperty(value = "Last name of the contact.")
    @Attribute(name = "sn")
    private String lastName;

    @ApiModelProperty(value = "Internet email address for the contact.")
    @Attribute(name = "mail")
    private String mail;

    @ApiModelProperty(value = "Mobile number of the contact. Should include landcode.")
    @Attribute(name = "mobile")
    private String mobile;

    @ApiModelProperty(value = "Indicates if the contact is the primary technical contact for the organisation.")
    @Attribute(name = "fintContactTechnical")
    private List<String> technical;

    @ApiModelProperty(value = "Indicates if the contact is the primary legal contact for the organisation.")
    @Attribute(name = "fintContactLegal")
    private List<String> legal;

    @ApiModelProperty(value = "Contacts id in the support system.")
    @Attribute(name = "fintContactSupportId")
    private String supportId;

    @ApiModelProperty(value = "Roles for the contact.")
    @Attribute(name = "fintContactRoles")
    private List<String> roles;

    public Contact() {
        technical = new ArrayList<>();
        legal = new ArrayList<>();
        roles = new ArrayList<>();
    }

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
        this.dn = LdapNameBuilder.newInstance(dn).build();
    }

    public void addOrganisationTechnicalContact(String organisationDn) {
        if (technical.stream().noneMatch(organisationDn::equalsIgnoreCase)) {
            technical.add(organisationDn);
        }
    }

    public void removeOrganisationTechnicalContact(String organisationDn) {
        technical.removeIf(organisationDn::equalsIgnoreCase);
    }

    public void addOrganisationLegalContact(String organisationDn) {
        if (legal.stream().noneMatch(organisationDn::equalsIgnoreCase)) {
            legal.add(organisationDn);
        }
    }

    public void removeOrganisationLegalContact(String organisationDn) {
        legal.removeIf(organisationDn::equalsIgnoreCase);
    }

    public void addRole(String role) {
        if (roles.stream().noneMatch(role::equalsIgnoreCase)) {
            roles.add(role);
        }
    }

    public void removeRole(String role) {
        roles.removeIf(role::equalsIgnoreCase);
    }
}
