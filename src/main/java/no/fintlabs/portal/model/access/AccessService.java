package no.fintlabs.portal.model.access;

import no.fintlabs.portal.ldap.LdapService;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.LdapConstants;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Objects;

@Service
public class AccessService {

    private final LdapService ldapService;
    private final AccessObjectService accessObjectService;
    private final ClientService clientService;

    public AccessService(LdapService ldapService, AccessObjectService accessObjectService, ClientService clientService) {
        this.ldapService = ldapService;
        this.accessObjectService = accessObjectService;
        this.clientService = clientService;
    }

    public List<AccessPackage> getAccesses(String orgName) {
        return ldapService.getAll(accessObjectService.getAccessBase(orgName).toString(), AccessPackage.class);
    }

    public boolean addAccess(AccessPackage accessPackage, Organisation organisation) {
        LdapName dn = LdapNameBuilder.newInstance(Objects.requireNonNull(organisation.getDn()))
                .add(LdapConstants.OU, "access")
                .add(LdapConstants.OU, accessPackage.getName())
                .build();
        accessPackage.setDn(dn);
        ldapService.createEntry(accessPackage);

        accessPackage.setSelf(dn);
        return ldapService.updateEntry(accessPackage);
    }

    public boolean updateAccess(AccessPackage accessPackage) {

        unlinkOldClients(accessPackage, getAccessByDn(accessPackage.getDn()).getClients());
        linkNewClients(accessPackage);

        return ldapService.updateEntry(accessPackage);
    }

    private void linkNewClients(AccessPackage accessPackage) {
        accessPackage.getClients().forEach(clientDn -> clientService.getClientByDn(clientDn).ifPresent(client ->
        {
            client.setAccessPackage(accessPackage.getDn());
            clientService.updateClient(client);
        }));
    }

    private void unlinkOldClients(AccessPackage accessPackage, List<String> oldClients) {
        oldClients.stream()
                .filter(c -> !accessPackage.getClients().contains(c))
                .forEach(clientDn -> clientService.getClientByDn(clientDn).ifPresent(client -> {
                    client.getAccessPackages().clear();
                    clientService.updateClient(client);
                }));

        accessPackage.getClients().stream()
                .filter(c -> !oldClients.contains(c))
                .map(clientService::getClientByDn)
                .forEach(client -> client.ifPresent(c -> {
                    c.getAccessPackages().stream()
                            .map(this::getAccessByDn)
                            .forEach(ap -> {
                                ap.getClients().remove(c.getDn());
                                ldapService.updateEntry(ap);
                            });
                }));
    }

    public void removeAccess(AccessPackage accessPackage) {
        ldapService.deleteEntry(accessPackage);
    }

    public void linkClientToAccess(AccessPackage accessPackage, Client client) {
        accessPackage.addClient(Objects.requireNonNull(client.getDn()));
        client.setAccessPackage(accessPackage.getDn());
        ldapService.updateEntry(accessPackage);
        ldapService.updateEntry(client);
    }

    public void unlinkClientFromAccess(AccessPackage accessPackage, Client client) {
        accessPackage.removeClient(client.getDn());
        client.getAccessPackages().clear();
        ldapService.updateEntry(accessPackage);
        ldapService.updateEntry(client);
    }

    public AccessPackage getAccess(String accessId, String orgName) {
        return getAccessByDn(accessObjectService.getAccessDn(accessId, orgName));
    }

    private AccessPackage getAccessByDn(String accessDn) {
        return ldapService.getEntry(accessDn, AccessPackage.class);
    }
}
