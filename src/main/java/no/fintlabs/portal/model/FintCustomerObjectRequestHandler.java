package no.fintlabs.portal.model;

import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;


public abstract class FintCustomerObjectRequestHandler<T extends BasicLdapEntry, E extends FintCustomerObjectEvent<T>> implements BiFunction<ConsumerRecord<String, E>, Organisation, T> {

    private final EntityTopicService entityTopicService;
    private final EntityTopicNameParameters entityTopic;

    private final EntityProducer<T> entityProducer;

    protected FintCustomerObjectRequestHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, Class<T> objectType) {
        this.entityTopicService = entityTopicService;

        entityProducer = entityProducerFactory.createProducer(getParameterClass());
        entityTopic = EntityTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .resource(objectType.getSimpleName().toLowerCase())
                .build();
    }

    @PostConstruct
    public void init() {
        entityTopicService.ensureTopic(entityTopic, 0);
    }

    public void send(T entity) {

        entityProducer.send(EntityProducerRecord
                .<T>builder()
                .key(entity.getDn())
                .topicNameParameters(entityTopic)
                .value(entity)
                .build()
        );
    }

    public void sendDelete(String dn) {
        entityProducer.send(EntityProducerRecord
                .<T>builder()
                .key(dn)
                .topicNameParameters(entityTopic)
                .value(null)
                .build()
        );
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterClass() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public abstract String operation();

}
