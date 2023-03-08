package no.fintlabs.portal.model.organisation;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
public class OrganisationConsumer {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;
    private final OrganisationService organisationService;

    public OrganisationConsumer(EventTopicService eventTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.organisationService = organisationService;
    }

    @PostConstruct
    public void init() {
        EventTopicNameParameters topic = EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("new-organisation")
                .build();
        eventTopicService.ensureTopic(topic,
                Duration.ofHours(48).toMillis());

        consumer.createFactory(Organisation.class, this::processEvent)
                .createContainer(topic);
    }

    private void processEvent(ConsumerRecord<String, Organisation> consumerRecord) {
        log.info("New organisation event received for : {}", consumerRecord.value().getDisplayName());
        Organisation organisation = organisationService.createOrganisation(consumerRecord.value());
        log.info("Created new organisation {}", organisation);
    }


}
