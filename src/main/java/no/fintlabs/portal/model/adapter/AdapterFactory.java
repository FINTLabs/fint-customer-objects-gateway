package no.fintlabs.portal.model.adapter;

import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.LdapConstants;
import no.fintlabs.portal.utilities.PasswordUtility;
import no.fintlabs.portal.utilities.SecretService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;

@Service
public class AdapterFactory {

    //@Value("${fint.ldap.organisation-base}")
    private final String organisationBase;

    private final SecretService secretService;

    public AdapterFactory(SecretService secretService, @Value("${fint.ldap.organisation-base}") String organisationBase) {
        this.secretService = secretService;
        this.organisationBase = organisationBase;
    }

    public void setupAdapter(Adapter adapter, Organisation organisation) {
        adapter.setName(String.format("%s@adapter.%s", adapter.getName(), organisation.getPrimaryAssetId()));
        adapter.setDn(
                LdapNameBuilder.newInstance(getAdapterBase(organisation.getName()))
                        .add(LdapConstants.CN, adapter.getName())
                        .build()
        );
        adapter.setPassword(secretService.generateSecret());
    }

    public Name getAdapterBase(String orgUuid) {
        return LdapNameBuilder.newInstance(organisationBase)
                .add(LdapConstants.OU, orgUuid)
                .add(LdapConstants.OU, LdapConstants.ADAPTER_CONTAINER_NAME)
                .build();
    }

    public String getAdapterDn(String adapterUuid, String orgUuid) {
        return LdapNameBuilder.newInstance(getAdapterBase(orgUuid))
                .add(LdapConstants.CN, adapterUuid)
                .build()
                .toString();
    }
}
