package no.fintlabs.portal.model.adapter;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.asset.AssetService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.oauth.NamOAuthClientService;
import no.fintlabs.portal.oauth.OAuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdapterService {

    @Autowired
    private AdapterObjectService adapterObjectService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private NamOAuthClientService namOAuthClientService;

    @Autowired
    private AssetService assetService;

    public boolean addAdapter(Adapter adapter, Organisation organisation) {
        adapterObjectService.setupAdapter(adapter, organisation);

        OAuthClient oAuthClient = namOAuthClientService.addOAuthClient(
                String.format("a_%s", adapter.getName()
                        .replace("@", "_")
                        .replace(".", "_")
                )
        );

        adapter.setClientId(oAuthClient.getClientId());

        boolean created = ldapService.createEntry(adapter);
        if (created) {
            Asset primaryAsset = assetService.getPrimaryAsset(organisation);
            assetService.linkAdapterToAsset(primaryAsset, adapter);
        }
        return created;
    }

    public List<Adapter> getAdapters(String orgName) {
        //List<Adapter> adapters =

        return ldapService.getAll(adapterObjectService.getAdapterBase(orgName).toString(), Adapter.class);

                /*
        adapters.forEach(adapter -> adapter.getAssets().forEach(asset -> {
            assetService.getAsset(asset).ifPresent(a -> adapter.addAssetId(a.getAssetId()));
        }));

        return adapters;
        */
    }

    public Optional<Adapter> getAdapter(String adapterName, String orgName) {

        //Optional<Adapter> adapter =

        return Optional.ofNullable(ldapService.getEntry(
                adapterObjectService.getAdapterDn(adapterName, orgName),
                Adapter.class
        ));

        /*
        adapter.ifPresent(a -> a.getAssets().forEach(asset -> {
            assetService.getAsset(asset).ifPresent(aa -> a.addAssetId(aa.getAssetId()));
        }));
        */

        //return adapter;
    }

    public Optional<Adapter> getAdapterByDn(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Adapter.class));
    }

    public String getAdapterSecret(Adapter adapter) {
        return namOAuthClientService.getOAuthClient(adapter.getClientId()).getClientSecret();
    }

    public boolean updateAdapter(Adapter adapter) {
        return ldapService.updateEntry(adapter);
    }

    public void deleteAdapter(Adapter adapter) {
        namOAuthClientService.removeOAuthClient(adapter.getClientId());
        ldapService.deleteEntry(adapter);
    }

    public void resetAdapterPassword(Adapter adapter, String newPassword) {
        adapter.setSecret(newPassword);
        ldapService.updateEntry(adapter);
    }

}
