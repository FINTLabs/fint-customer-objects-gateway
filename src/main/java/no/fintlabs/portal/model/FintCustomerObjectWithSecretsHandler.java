package no.fintlabs.portal.model;

import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;

public abstract class FintCustomerObjectWithSecretsHandler<T extends BasicLdapEntryWithSecrets, E extends FintCustomerObjectEvent<T>, S extends FintCustomerObjectWithSecretsService<T>> extends FintCustomerObjectHandler<T, E> {


    protected final S objectService;

    protected FintCustomerObjectWithSecretsHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, Class<T> objectType, /*FintCustomerObjectWithSecretsCacheRepository<T> cacheRepository,*/ S objectService) {
        super(entityTopicService, entityProducerFactory, objectType);
        this.objectService = objectService;
    }
}
