package no.fintlabs.portal.model.asset;

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
@Entry(objectClasses = {"organizationalUnit", "top", "fintAsset"})
public final class Asset implements BasicLdapEntry {

    @Id
    private Name dn;

    @ApiModelProperty(value = "Technical name of the component.")
    @Attribute(name = "ou")
    private String name;

    @ApiModelProperty(value = "A description of what the component does.")
    @Attribute(name = "description")
    private String description;

    @Attribute(name = "fintAssetOrganisation")
    private String organisation;

    @Attribute(name = "fintAssetClients")
    private List<String> clients;

    @Attribute(name = "fintAssetAdapters")
    private List<String> adapters;

    @Attribute(name = "fintAssetId")
    private String assetId;

    @Attribute(name = "fintAssetPrimary")
    private boolean primaryAsset;

    public Asset() {
        clients = new ArrayList<>();
        adapters = new ArrayList<>();
    }

    public String getOrganisation() {
        return organisation;
    }

    public List<String> getClients() {
        return clients;
    }

    public void addClient(String clientDn) {
        if (clients.stream().noneMatch(clientDn::equalsIgnoreCase)) {
            clients.add(clientDn);
        }
    }

    public void removeAdapter(String adapterDn) {
        adapters.removeIf(adapter -> adapter.equalsIgnoreCase(adapterDn));
    }

    public void addAdapter(String adapterDn) {
        if (adapters.stream().noneMatch(adapterDn::equalsIgnoreCase)) {
            adapters.add(adapterDn);
        }
    }

    public void removeClient(String clientDn) {
        clients.removeIf(client -> client.equalsIgnoreCase(clientDn));
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

