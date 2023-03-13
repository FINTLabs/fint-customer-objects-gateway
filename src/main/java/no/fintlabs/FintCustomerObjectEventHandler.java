package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.function.Function;

@Slf4j
public abstract class FintCustomerObjectEventHandler<T extends FintCustomerObjectEvent<O>, O extends BasicLdapEntry> {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;

    private final OrganisationService organisationService;

    private final EventProducer<T> eventProducer;
    private final EventTopicNameParameters organisationCreatedTopic;

    private final String eventType;

    private final String objectType;

    public FintCustomerObjectEventHandler(EventTopicService eventTopicService,
                                          EventConsumerFactoryService consumer,
                                          OrganisationService organisationService,
                                          EventProducerFactory eventProducerFactory,
                                          Class<O> objectType,
                                          String eventType) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.organisationService = organisationService;
        this.eventType = eventType;
        this.objectType = objectType.getSimpleName().toLowerCase();

        eventProducer = eventProducerFactory.createProducer(getParameterClass());
        organisationCreatedTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName(this.objectType)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterClass() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @PostConstruct
    public void init() {
        EventTopicNameParameters createClientTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName(eventType + "-" + objectType)
                .build();
        eventTopicService.ensureTopic(createClientTopic, Duration.ofHours(48).toMillis());
        eventTopicService.ensureTopic(organisationCreatedTopic, Duration.ofHours(48).toMillis());

        consumer.createFactory(getParameterClass(), this::processEvent)
                .createContainer(createClientTopic);

    }

    private void processEvent(ConsumerRecord<String, T> consumerRecord) {

        log.info("Event received for : {}", consumerRecord.value().getObject());
        final T response = consumerRecord.value();
        try {
            organisationService
                    .getOrganisation(consumerRecord.value().getOrganisationObjectName())
                    .map(handleObject(consumerRecord, response))
                    .orElseThrow(() -> new RuntimeException("Unable to find organisation"));
        } catch (Exception e) {
            response.setStatus(FintCustomerObjectEvent.Status.builder().successful(false).message(e.getMessage()).build());
        }

        eventProducer.send(EventProducerRecord
                .<T>builder()
                .topicNameParameters(organisationCreatedTopic)
                .value(response)
                .build()
        );
    }

    public abstract Function<Organisation, T> handleObject(ConsumerRecord<String, T> consumerRecord, T response);
}
