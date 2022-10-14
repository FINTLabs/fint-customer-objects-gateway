package no.fintlabs.portal.model.asset;

import no.fintlabs.portal.exceptions.CreateEntityMismatchException;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.LdapConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private LdapService ldapService;

    private boolean createAsset(Asset asset, Organisation organisation, boolean primary) {
        asset.setName(asset.getAssetId().replace(".", "_"));
        asset.setDn(
                LdapNameBuilder.newInstance(organisation.getDn())
                        .add(LdapConstants.OU, LdapConstants.ASSET_CONTAINER_NAME)
                        .add(LdapConstants.OU, asset.getName())
                        .build()
        );
        asset.setOrganisation(organisation.getDn());
        asset.setPrimaryAsset(primary);


        return ldapService.createEntry(asset);
    }

    public void addSubAsset(Asset asset, Organisation organisation) {
        Asset primaryAsset = getPrimaryAsset(organisation);
        asset.setAssetId(String.format("%s.%s", asset.getAssetId(), primaryAsset.getAssetId()));

        if (isIllegalAssetID(asset.getAssetId()))
            throw new IllegalArgumentException("The assetId contains illegal characters: " + asset.getAssetId());

        if (!createAsset(asset, organisation, false))
            throw new CreateEntityMismatchException(asset.getAssetId());
    }

    public boolean addPrimaryAsset(Asset asset, Organisation organisation) {
        return createAsset(asset, organisation, true);
    }

    public void removeAsset(Asset asset) {
        ldapService.deleteEntry(asset);
    }

    public boolean updateAsset(Asset asset) {
        return ldapService.updateEntry(asset);
    }

    public void linkClientToAsset(Asset asset, Client client) {

        asset.addClient(client.getDn());
        client.setAssetId(asset.getAssetId());
        client.setAsset(asset.getDn());

        ldapService.updateEntry(asset);
        ldapService.updateEntry(client);
    }

    public void unlinkClientFromAsset(Asset asset, Client client) {

        asset.removeClient(client.getDn());
        client.setAssetId(null);
        client.setAsset(null);

        ldapService.updateEntry(asset);
        ldapService.updateEntry(client);
    }

    public void linkAdapterToAsset(Asset asset, Adapter adapter) {

        asset.addAdapter(adapter.getDn());
        adapter.addAsset(asset.getDn());
        adapter.addAssetId(asset.getAssetId());

        ldapService.updateEntry(asset);
        ldapService.updateEntry(adapter);
    }

    public void unlinkAdapterFromAsset(Asset asset, Adapter adapter) {

        asset.removeAdapter(adapter.getDn());
        adapter.removeAsset(asset.getDn());
        adapter.removeAssetId(asset.getAssetId());

        ldapService.updateEntry(asset);
        ldapService.updateEntry(adapter);
    }

    public List<Asset> getAssets(Organisation organisation) {

        return ldapService.getAll(LdapNameBuilder.newInstance(organisation.getDn())
                        .add(LdapConstants.OU, LdapConstants.ASSET_CONTAINER_NAME)
                        .build().toString(),
                Asset.class);
    }

    public Optional<Asset> getAsset(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Asset.class));
    }

    public Asset getPrimaryAsset(Organisation organisation) {
        return getAssets(organisation).stream().filter(asset -> asset.isPrimaryAsset()).findFirst().orElse(new Asset());
    }

    private boolean isIllegalAssetID(String assetId) {
        return StringUtils.isBlank(assetId)
                || !StringUtils.isAsciiPrintable(assetId)
                || StringUtils.containsAny(assetId, " !\"#$%&'()*+,/:;<=>?@[\\]^`{}|~");
    }
}
