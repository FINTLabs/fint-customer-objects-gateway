package no.fintlabs;

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


public abstract class FintCustomerObjectEntityHandler<T extends BasicLdapEntry, E extends FintCustomerObjectEvent<T>> implements BiConsumer<ConsumerRecord<String, E>, Organisation> {

    private final EntityTopicService entityTopicService;
    private final EntityTopicNameParameters entityTopic;

    private final EntityProducer<T> entityProducer;

    protected FintCustomerObjectEntityHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, Class<T> objectType) {
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

//    public String getEntityType() {
//        return getParameterClass().getSimpleName().toUpperCase();
//    }

    public abstract String operation();

}
