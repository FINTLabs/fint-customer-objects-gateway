package no.fintlabs.portal.model.component;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.asset.AssetService;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.organisation.OrganisationService;
import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComponentService {

    @Autowired
    private LdapService ldapService;

    @Autowired
    private ComponentObjectService componentObjectService;

//    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private AssetService assetService;

    @Value("${fint.ldap.component-base}")
    private String componentBase;


    @Autowired
    public void setOrganisationService(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    public OrganisationService getOrganisationService() {
        return this.organisationService;
    }

    public boolean createComponent(Component component) {
        log.info("Creating component: {}", component);

        if (component.getDn() == null) {
            componentObjectService.setupComponent(component);
        }
        return ldapService.createEntry(component);
    }

    public boolean updateComponent(Component component) {
        log.info("Updating component: {}", component);

        return ldapService.updateEntry(component);
    }

    public List<Component> getComponents() {
        return ldapService.getAll(componentBase, Component.class);
    }

    public Optional<Component> getComponentByName(String name) {
        return getComponetByDn(getComponentDnByName(name));
    }

    public Optional<Component> getComponetByDn(String dn) {
        return Optional.ofNullable(ldapService.getEntry(dn, Component.class));
    }

    public void deleteComponent(Component component) {
        ldapService.deleteEntry(component);
    }

    public String getComponentDnByName(String name) {
        if (name != null) {
            return LdapNameBuilder.newInstance(componentBase)
                    .add(LdapConstants.OU, name)
                    .build().toString();
        }
        return null;
    }

    public void linkClient(Component component, Client client) {

        component.addClient(client.getDn());
        client.addComponent(component.getDn());
        ldapService.updateEntry(client);
        ldapService.updateEntry(component);
    }

    public void unLinkClient(Component component, Client client) {

        component.removeClient(client.getDn());
        client.removeComponent(component.getDn());

        ldapService.updateEntry(client);
        ldapService.updateEntry(component);
    }

    public void linkAdapter(Component component, Adapter adapter) {

        component.addAdapter(adapter.getDn());
        adapter.addComponent(component.getDn());

        ldapService.updateEntry(adapter);
        ldapService.updateEntry(component);
    }

    public void unLinkAdapter(Component component, Adapter adapter) {

        component.removeAdapter(adapter.getDn());
        adapter.removeComponent(component.getDn());

        ldapService.updateEntry(adapter);
        ldapService.updateEntry(component);
    }

    public List<Asset> getActiveAssetsForComponent(Component component) {
        return component
                .getOrganisations()
                .stream()
                .map(organisationService::getOrganisationByDn)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(assetService::getAssets)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
