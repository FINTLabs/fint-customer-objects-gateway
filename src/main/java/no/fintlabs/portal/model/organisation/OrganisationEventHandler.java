package no.fintlabs.portal.model.organisation;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
//@Component
public class OrganisationEventHandler {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;
    private final OrganisationService organisationService;

    private final EventProducer<Organisation> eventProducer;
    private final EventTopicNameParameters organisationCreatedTopic;

    public OrganisationEventHandler(EventTopicService eventTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, EventProducerFactory eventProducerFactory) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.organisationService = organisationService;

        eventProducer = eventProducerFactory.createProducer(Organisation.class);
        organisationCreatedTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("organisation-created")
                .build();


    }


    @PostConstruct
    public void init() {
        EventTopicNameParameters createOrganisationTopic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("new-organisation")
                .build();
        eventTopicService.ensureTopic(createOrganisationTopic, Duration.ofHours(48).toMillis());
        eventTopicService.ensureTopic(organisationCreatedTopic, Duration.ofHours(48).toMillis());

        consumer.createFactory(Organisation.class, this::processEvent)
                .createContainer(createOrganisationTopic);

    }

    private void processEvent(ConsumerRecord<String, Organisation> consumerRecord) {
        log.info("New organisation event received for : {}", consumerRecord.value().getDisplayName());
        Organisation organisation = organisationService.createOrganisation(consumerRecord.value());
        eventProducer.send(EventProducerRecord
                .<Organisation>builder()
                .topicNameParameters(organisationCreatedTopic)
                .value(organisation)
                .build()
        );
        log.info("Created new organisation {}", organisation);
    }


}
