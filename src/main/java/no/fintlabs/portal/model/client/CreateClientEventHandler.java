package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FintCustomerObjectEvent;
import no.fintlabs.FintCustomerObjectEventHandler;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class CreateClientEventHandler extends FintCustomerObjectEventHandler<ClientEvent, Client> {

    private final ClientService clientService;

    public CreateClientEventHandler(EventTopicService eventTopicService,
                                    EventConsumerFactoryService consumer,
                                    OrganisationService organisationService,
                                    EventProducerFactory eventProducerFactory,
                                    ClientService clientService) {
        super(eventTopicService, consumer, organisationService, eventProducerFactory, Client.class, "create");
        this.clientService = clientService;
    }

    @Override
    public Function<Organisation, ClientEvent> handleObject(ConsumerRecord<String, ClientEvent> consumerRecord, ClientEvent response) {
        return organisation -> clientService.addClient(consumerRecord.value().getObject(), organisation)
                .map(client -> {
                    response.setObject(client);
                    response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
                    return response;
                })
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));
    }
}
