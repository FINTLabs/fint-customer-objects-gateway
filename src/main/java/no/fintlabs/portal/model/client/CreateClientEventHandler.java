package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FintCustomerObjectEvent;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
public class CreateClientEventHandler {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;
    private final ClientService clientService;

    private final OrganisationService organisationService;

    private final EventProducer<ClientEvent> eventProducer;
    private final EventTopicNameParameters organisationCreatedTopic;

    public CreateClientEventHandler(EventTopicService eventTopicService, EventConsumerFactoryService consumer, ClientService clientService, OrganisationService organisationService, EventProducerFactory eventProducerFactory) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.clientService = clientService;
        this.organisationService = organisationService;

        eventProducer = eventProducerFactory.createProducer(ClientEvent.class);
        organisationCreatedTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("client")
                .build();


    }


    @PostConstruct
    public void init() {
        EventTopicNameParameters createClientTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("create-client")
                .build();
        eventTopicService.ensureTopic(createClientTopic, Duration.ofHours(48).toMillis());
        eventTopicService.ensureTopic(organisationCreatedTopic, Duration.ofHours(48).toMillis());

        consumer.createFactory(ClientEvent.class, this::processEvent)
                .createContainer(createClientTopic);

    }

    private void processEvent(ConsumerRecord<String, ClientEvent> consumerRecord) {

        log.info("Event received for : {}", consumerRecord.value().getObject());
        final ClientEvent response = consumerRecord.value();
        try {
            organisationService
                    .getOrganisation(consumerRecord.value().getOrganisationObjectName())
                    .map(organisation -> clientService.addClient(consumerRecord.value().getObject(), organisation)
                            .map(client -> {
                                response.setObject(client);
                                response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
                                return response;
                            })
                            .orElseThrow(() -> new RuntimeException("An excepted error occurred while creating client.")))
                    .orElseThrow(() -> new RuntimeException("Unable to find organisation"));
        } catch (Exception e) {
            response.setStatus(FintCustomerObjectEvent.Status.builder().successful(false).message(e.getMessage()).build());
        }

        eventProducer.send(EventProducerRecord
                .<ClientEvent>builder()
                .topicNameParameters(organisationCreatedTopic)
                .value(response)
                .build()
        );
    }


}
