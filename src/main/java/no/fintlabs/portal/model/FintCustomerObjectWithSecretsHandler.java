package no.fintlabs.portal.model;

import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Optional;

public abstract class FintCustomerObjectWithSecretsHandler<T extends BasicLdapEntryWithSecrets, E extends FintCustomerObjectEvent<T>, S extends FintCustomerObjectWithSecretsService<T>> extends FintCustomerObjectHandler<T, E> {

    //private final FintCustomerObjectWithSecretsCacheRepository<T> cacheRepository;

    protected final S objectService;

    protected FintCustomerObjectWithSecretsHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, Class<T> objectType, /*FintCustomerObjectWithSecretsCacheRepository<T> cacheRepository,*/ S objectService) {
        super(entityTopicService, entityProducerFactory, objectType);
        //this.cacheRepository = cacheRepository;
        this.objectService = objectService;
    }

    protected void ensureSecrets(ConsumerRecord<String, E> consumerRecord, T object) {
        object.setPublicKey(consumerRecord.value().getObject().getPublicKey());
        objectService.encryptPassword(object, consumerRecord.value().getObject().getPublicKey());
        objectService.encryptClientSecret(object, consumerRecord.value().getObject().getPublicKey());
    }

//    protected void addToCache(T object) {
//        cacheRepository.add(object);
//    }
//
//    protected Optional<T> getFromCache(T object) {
//
//        return cacheRepository.get(object);
//    }
//
//    protected void updateCache(T object) {
//        cacheRepository.update(object);
//    }
//
//    protected void removeFromCache(T object) {
//        cacheRepository.remove(object);
//    }
}
