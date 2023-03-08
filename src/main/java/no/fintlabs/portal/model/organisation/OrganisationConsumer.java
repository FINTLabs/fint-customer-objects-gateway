package no.fintlabs.portal.model.organisation;

import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
public class OrganisationConsumer {

    private final EventTopicService eventTopicService;

    public OrganisationConsumer(EventTopicService eventTopicService) {
        this.eventTopicService = eventTopicService;
    }

    @PostConstruct
    public void init() {
        eventTopicService.ensureTopic(EventTopicNameParameters
                .builder()
                .orgId("flais.io")       // Optional if set as application property
                .domainContext("fint-service")  // Optional if set as application property
                .eventName("new-organisation")
                .build(),
                Duration.ofHours(48).toMillis());
    }
}
