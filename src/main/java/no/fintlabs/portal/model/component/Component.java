package no.fintlabs.portal.model.component;

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
@Entry(objectClasses = {"organizationalUnit", "top", "fintComponent"})
public final class Component implements BasicLdapEntry {

    @Id
    private Name dn;

    @ApiModelProperty(value = "Technical name of the component.")
    @Attribute(name = "ou")
    private String name;

    @ApiModelProperty(value = "A description of what the component does.")
    @Attribute(name = "description")
    private String description;

    @Attribute(name = "fintComponentOrganisations")
    private List<String> organisations;

    @Attribute(name = "fintComponentClients")
    private List<String> clients;

    @Attribute(name = "fintComponentAdapters")
    private List<String> adapters;

    @Attribute(name = "fintComponentBasePath")
    private String basePath;

    @Deprecated
    @Attribute(name = "fintComponentPort")
    private Integer port;

    @Attribute(name = "fintComponentCore")
    private boolean core;

    @Attribute(name = "fintComponentOpenData")
    private boolean openData;

    @Attribute(name = "fintComponentCommon")
    private boolean common;

    @Attribute(name = "fintComponentIsInProduction")
    private boolean isInProduction;

    @Attribute(name = "fintComponentIsInBeta")
    private boolean isInBeta;

    @Attribute(name = "fintComponentIsInPlayWithFint")
    private boolean isInPlayWithFint;

    @Attribute(name = "fintComponentDockerImage")
    private String dockerImage;

    @Attribute(name = "fintComponentSizes")
    private String componentSizes;

    @Attribute(name = "fintComponentCacheDisabledFor")
    private List<String> cacheDisabledFor;

    public Component() {

        organisations = new ArrayList<>();
        clients = new ArrayList<>();
        adapters = new ArrayList<>();
        cacheDisabledFor = new ArrayList<>();
    }

    public List<String> getOrganisations() {
        return organisations;
    }

    public List<String> getClients() {
        return clients;
    }

    public List<String> getAdapters() {
        return adapters;
    }

    public void addOrganisation(String organisationDn) {
        if (organisations.stream().noneMatch(organisationDn::equalsIgnoreCase)) {
            organisations.add(organisationDn);
        }
    }

    public void removeOrganisation(String organisationDn) {
        organisations.removeIf(component -> component.equalsIgnoreCase(organisationDn));
    }

    public void addClient(String clientDn) {
        if (clients.stream().noneMatch(clientDn::equalsIgnoreCase)) {
            clients.add(clientDn);
        }
    }

    public void removeClient(String clientDn) {
        clients.removeIf(client -> client.equalsIgnoreCase(clientDn));
    }

    public void addAdapter(String adapterDn) {
        if (adapters.stream().noneMatch(adapterDn::equalsIgnoreCase)) {
            adapters.add(adapterDn);
        }
    }

    public void removeAdapter(String adapterDn) {
        adapters.removeIf(adapter -> adapter.equalsIgnoreCase(adapterDn));
    }

    public List<String> getCacheDisabledFor() {
        return cacheDisabledFor;
    }

    public void setCacheDisabledFor(String entity) {
        if (cacheDisabledFor.stream().noneMatch(entity::equalsIgnoreCase)) {
            cacheDisabledFor.add(entity);
        }
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
    public void setDn(String dn) {
        this.dn = LdapNameBuilder.newInstance(dn).build();
    }

    @Override
    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

