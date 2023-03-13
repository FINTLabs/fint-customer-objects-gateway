package no.fintlabs.portal.model.client;

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

@Component
public class GetClientHandler extends FintCustomerObjectEventHandler<ClientEvent, Client> {
    private final ClientService clientService;
    public GetClientHandler(EventTopicService eventTopicService,
                            EventConsumerFactoryService consumer,
                            OrganisationService organisationService,
                            EventProducerFactory eventProducerFactory,

                            ClientService clientService) {
        super(eventTopicService, consumer, organisationService, eventProducerFactory, Client.class, "get");
        this.clientService = clientService;
    }

    @Override
    public Function<Organisation, ClientEvent> handleObject(ConsumerRecord<String, ClientEvent> consumerRecord, ClientEvent response) {
        return organisation -> clientService.getClient(consumerRecord.value().getObject().getName(), organisation.getName())
                .map(client -> {
                    response.setObject(client);
                    response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
                    return response;
                })
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while getting client."));
    }
}
