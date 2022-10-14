package no.fintlabs.portal.model.organisation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ALL")
@ApiModel
@Data
@Entry(objectClasses = {"organizationalUnit", "top", "fintOrganisation"})
public final class Organisation implements BasicLdapEntry {

    @Id
    private Name dn;

    @ApiModelProperty(
            value = "Unique identifier for the organisation (UUID). This is automatically generated and should not be set."
    )
    @Attribute(name = "ou")
    private String name;

    @ApiModelProperty(value = "The organisation number from Enhetsregisteret (https://w2.brreg.no/enhet/sok/index.jsp)")
    @Attribute(name = "fintOrganisationNumber")
    private String orgNumber;

    @ApiModelProperty(
            value = "The official name of the organisation. See Enhetsregisteret (https://w2.brreg.no/enhet/sok/index.jsp)"
    )
    @Attribute(name = "fintOrganisationDisplayName")
    private String displayName;

    @Attribute(name = "fintOrganisationComponents")
    private List<String> components;

    @Attribute(name = "fintOrganisationLegal")
    private String legalContact;

    @Attribute(name = "fintOrganisationTechnical")
    private List<String> techicalContacts;

    @Attribute(name = "fintOrganisationK8sSize")
    private String k8sSize;

    @ApiModelProperty(
            value = "Indicates if the organistion is a customer. Typically if it's a customer k8s deployments should be created."
    )
    @Attribute(name = "fintOrganisationCustomer")
    private boolean customer;

    @Transient
    private String primaryAssetId;

    public Organisation() {
        components = new ArrayList<>();
        techicalContacts = new ArrayList<>();
    }

    public void addComponent(String componentDn) {
        if (!components.stream().anyMatch(componentDn::equalsIgnoreCase)) {
            components.add(componentDn);
        }
    }

    public void removeComponent(String componentDn) {
        components.removeIf(componentDn::equalsIgnoreCase);
    }

    public void addTechnicalContact(String contactDn) {
        if (!techicalContacts.stream().anyMatch(contactDn::equalsIgnoreCase)) {
            techicalContacts.add(contactDn);
        }
    }

    public void removeTechicalContact(String contactDn) {
        techicalContacts.removeIf(contactDn::equalsIgnoreCase);
    }

    public void setName(String name) {
        this.name = name;
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
        this.dn = LdapNameBuilder.newInstance(dn).build();
    }
}
