package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FintCustomerObjectEntityHandler;
import no.fintlabs.FintCustomerObjectEvent;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateHandler extends FintCustomerObjectEntityHandler<Client, ClientEvent> {

    private final ClientService clientService;

    protected CreateHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
        super(entityTopicService, entityProducerFactory, Client.class);
        this.clientService = clientService;
    }


    @Override
    public FintCustomerObjectEvent.Operation operation() {
        return FintCustomerObjectEvent.Operation.CREATE;
    }

    @Override
    public void accept(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        Client client = clientService.addClient(consumerRecord.value().getObject(), organisation)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));
        send(client);
    }


    //@Override
    //public void accept(FintCustomerObjectEvent<Client> clientFintCustomerObjectEvent) {
//        clientService.addClient(consumerRecord.value().getObject(), organisation)
//                .map(client -> {
//                    response.setObject(client);
//                    response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
//                    return response;
//                })
//                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));
    //}

//    public CreateClientEventHandler(EventTopicService eventTopicService, EntityTopicService entityTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
//        super(eventTopicService, entityTopicService, consumer, organisationService, entityProducerFactory, handlers, Client.class);
//        this.clientService = clientService;
//    }
//
//
//    @Override
//    public Function<Organisation, ClientEvent> handleObject(ConsumerRecord<String, ClientEvent> consumerRecord, ClientEvent response) {
//        return organisation -> clientService.addClient(consumerRecord.value().getObject(), organisation)
//                .map(client -> {
//                    response.setObject(client);
//                    response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
//                    return response;
//                })
//                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));
//    }
}
