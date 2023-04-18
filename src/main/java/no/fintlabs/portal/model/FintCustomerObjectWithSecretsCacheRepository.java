package no.fintlabs.portal.model;

import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public abstract class FintCustomerObjectWithSecretsCacheRepository<T extends BasicLdapEntryWithSecrets> {

    private final ConcurrentHashMap<String, T> clients = new ConcurrentHashMap<>();

    public void add(T client) {
        clients.put(client.getDn(), client);
    }

    public Optional<T> get(T client) {
        T client1 = clients.get(client.getDn());
        if (client1 != null && client1.getPublicKey().equals(client.getPublicKey())) {
            return Optional.of(client1);
        }
        return Optional.empty();
    }

    public void update(T client) {
        clients.put(client.getDn(), client);
    }

    public void remove(T client) {
        clients.remove(client.getDn());
    }

    public int size() {
        return clients.size();
    }

    public Collection<T> objects() {
        return clients.values();
    }
}
