package no.fintlabs.portal.model.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@ToString(exclude = {"password", "clientSecret"})
@Entry(objectClasses = {"fintAdapter", "inetOrgPerson", "organizationalPerson", "person", "top"})
@Entity
@Table(name = "adapter")
public final class Adapter implements BasicLdapEntryWithSecrets {

    @Id
    @javax.persistence.Id
    private Name dn;

    @Getter
    @Setter
    @Attribute(name = "cn")
    private String name;

    @Getter
    @Setter
    @Attribute(name = "fintAdapterManaged")
    private boolean isManaged;

    @Getter
    @Setter
    @Attribute(name = "sn")
    private String shortDescription;

    @Getter
    @Setter
    @Attribute(name = "fintAdapterAssets")
    @ElementCollection
    private List<String> assets;

    @Getter
    @Setter
    @Attribute(name = "fintAdapterAssetIds")
    @ElementCollection
    private List<String> assetIds;

    @Getter
    @Setter
    @Attribute(name = "description")
    @Column(length = 4096)
    private String note;

    @Getter
    @Setter
    @Transient
    @Column(length = 512)
    private String password;

    @Getter
    @Setter
    @Transient
    @Column(length = 512)
    private String clientSecret;

    @Transient
    @Getter
    @Setter
    @Column(length = 512)
    private String publicKey;

    @Getter
    @Setter
    @Attribute(name = "fintOAuthClientId")
    private String clientId;

    @Getter
    @Attribute(name = "fintAdapterComponents")
    @ElementCollection
    private List<String> components;

//    @Getter
//    @Attribute(name = "fintAdapterAccessPackages")
//    @ElementCollection
//    private List<String> accessPackages;

    public Adapter() {
        components = new ArrayList<>();
        assets = new ArrayList<>();
        assetIds = new ArrayList<>();
    }

    public void addComponent(String componentDn) {
        if (components.stream().noneMatch(componentDn::equalsIgnoreCase)) {
            components.add(componentDn);
        }
    }

    public void removeComponent(String componentDn) {
        components.removeIf(component -> component.equalsIgnoreCase(componentDn));
    }

    public void addAssetId(String assetId) {
        if (assetIds.stream().noneMatch(assetId::equalsIgnoreCase)) {
            assetIds.add(assetId);
        }
    }

    public void addAsset(String assetId) {
        if (assets.stream().noneMatch(assetId::equalsIgnoreCase)) {
            assets.add(assetId);
        }
    }

    public void removeAsset(String assetId) {
        assets.removeIf(asset -> asset.equalsIgnoreCase(assetId));
    }

    public void removeAssetId(String assetId) {
        assetIds.removeIf(asset -> asset.equalsIgnoreCase(assetId));
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

    @Override
    public void clearSecrets() {
        password = null;
        clientSecret = null;
    }

}