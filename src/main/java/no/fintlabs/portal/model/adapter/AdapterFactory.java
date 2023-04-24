package no.fintlabs.portal.model.adapter;

import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class AdapterFactory {
    private final String organisationBase;

    public AdapterFactory(@Value("${fint.ldap.organisation-base}") String organisationBase) {
        this.organisationBase = organisationBase;
    }

    public void setupAdapter(Adapter adapter, Organisation organisation) {
        adapter.setName(getAdapterFullName(adapter.getName(), organisation.getPrimaryAssetId()));
        adapter.setDn(
                LdapNameBuilder.newInstance(getAdapterBase(organisation.getName()))
                        .add(LdapConstants.CN, adapter.getName())
                        .build()
        );
    }

    private String getAdapterFullName(String adapterSimpleName, String organisationPrimaryAssetId) {
        if (adapterSimpleName.contains("@")) {
            return adapterSimpleName;
        }
        return String.format("%s@adapter.%s", adapterSimpleName, organisationPrimaryAssetId);
    }

    public Name getAdapterBase(String orgUuid) {
        return LdapNameBuilder.newInstance(organisationBase)
                .add(LdapConstants.OU, orgUuid)
                .add(LdapConstants.OU, LdapConstants.ADAPTER_CONTAINER_NAME)
                .build();
    }

    public String getAdapterDn(String adapterName, Organisation organisation) {
        return LdapNameBuilder.newInstance(getAdapterBase(organisation.getName()))
                .add(LdapConstants.CN, getAdapterFullName(adapterName, organisation.getPrimaryAssetId()))
                .build()
                .toString();
    }
}
