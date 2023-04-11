package no.fintlabs.portal.model;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class FintCustomerObjectEventHandler<E extends FintCustomerObjectEvent<T>, T extends BasicLdapEntry> {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;

    private final OrganisationService organisationService;

    private Map<String, FintCustomerObjectEntityHandler<T, E>> actionsHandlerMap;
    private final Collection<FintCustomerObjectEntityHandler<T, E>> handlers;


    private final String objectType;

    public FintCustomerObjectEventHandler(EventTopicService eventTopicService,
                                          EventConsumerFactoryService consumer,
                                          OrganisationService organisationService,
                                          Collection<FintCustomerObjectEntityHandler<T, E>> handlers, Class<T> objectType) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.organisationService = organisationService;
        this.handlers = handlers;
        this.objectType = objectType.getSimpleName().toLowerCase();


    }

    @SuppressWarnings("unchecked")
    private Class<E> getParameterClass() {
        return (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @PostConstruct
    public void init() {
        EventTopicNameParameters createClientTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName(objectType)
                .build();
        eventTopicService.ensureTopic(createClientTopic, Duration.ofHours(48).toMillis());

        consumer.createFactory(getParameterClass(), this::processEvent)
                .createContainer(createClientTopic);

        actionsHandlerMap = new HashMap<>();
        handlers
                .forEach(fintCustomerObjectEntityHandler ->
                        actionsHandlerMap.put(
                                fintCustomerObjectEntityHandler.operation(),
                                fintCustomerObjectEntityHandler)
                );
        log.info("Registered {} handlers", handlers.size());

    }

    private void processEvent(ConsumerRecord<String, E> consumerRecord) {

        log.info("Event received for : {}", consumerRecord.value().getObject());
        try {
            actionsHandlerMap
                    .get(consumerRecord.value().getOperationWithType())
                    .accept(consumerRecord,
                            organisationService
                                    .getOrganisation(consumerRecord.value().getOrganisationObjectName())
                                    .orElseThrow(() -> new RuntimeException("Unable to find organisation " + consumerRecord.value().getOrgId()))
                    );

        } catch (Exception e) {
            log.error("An error occurred when handling event {}:", consumerRecord.value().getObject());
            log.error(e.getMessage());
        }

    }


}
