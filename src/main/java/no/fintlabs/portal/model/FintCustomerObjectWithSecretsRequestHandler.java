package no.fintlabs.portal.model;

import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;

import java.util.Optional;

public abstract class FintCustomerObjectWithSecretsRequestHandler<T extends BasicLdapEntryWithSecrets, E extends FintCustomerObjectEvent<T>> extends FintCustomerObjectRequestHandler<T, E> {

    private final FintCustomerObjectWithSecretsCacheRepository<T> cacheRepository;

    protected FintCustomerObjectWithSecretsRequestHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, Class<T> objectType, FintCustomerObjectWithSecretsCacheRepository<T> cacheRepository) {
        super(entityTopicService, entityProducerFactory, objectType);
        this.cacheRepository = cacheRepository;
    }

    protected void addToCache(T object) {
        cacheRepository.add(object);
    }

    protected Optional<T> getFromCache(T object) {

        return cacheRepository.get(object);
    }

    protected void updateCache(T object) {
        cacheRepository.update(object);
    }

    protected void removeFromCache(T object) {
        cacheRepository.remove(object);
    }
}
