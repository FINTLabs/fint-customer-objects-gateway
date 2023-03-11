package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FintCustomerObjectRequest;
import no.fintlabs.FintCustomerObjectResponse;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
public class GetClientEventHandler {

    private final EventTopicService eventTopicService;
    private final EventConsumerFactoryService consumer;
    private final ClientService clientService;

    private final EventProducer<FintCustomerObjectResponse> eventProducer;
    private final EventTopicNameParameters organisationCreatedTopic;

    public GetClientEventHandler(EventTopicService eventTopicService, EventConsumerFactoryService consumer, ClientService clientService, EventProducerFactory eventProducerFactory) {
        this.eventTopicService = eventTopicService;
        this.consumer = consumer;
        this.clientService = clientService;

        eventProducer = eventProducerFactory.createProducer(FintCustomerObjectResponse.class);
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
                .eventName("get-client")
                .build();
        eventTopicService.ensureTopic(createClientTopic, Duration.ofHours(48).toMillis());
        eventTopicService.ensureTopic(organisationCreatedTopic, Duration.ofHours(48).toMillis());

        consumer.createFactory(FintCustomerObjectRequest.class, this::processEvent)
                .createContainer(createClientTopic);

    }

    private void processEvent(ConsumerRecord<String, FintCustomerObjectRequest> consumerRecord) {
        log.info("Event received for : {}", consumerRecord.value().getObjectId());
        FintCustomerObjectResponse<Client> response = clientService.getClientByDn(
                        consumerRecord.value().getObjectId())
                .map(client -> FintCustomerObjectResponse.<Client>builder().successful(true).customerObject(client).build())
                .orElse(FintCustomerObjectResponse.<Client>builder().successful(false).message("Shit granit 🧨").build());
        eventProducer.send(EventProducerRecord
                .<FintCustomerObjectResponse>builder()
                .topicNameParameters(organisationCreatedTopic)
                .value(response)
                .build()
        );
    }


}
