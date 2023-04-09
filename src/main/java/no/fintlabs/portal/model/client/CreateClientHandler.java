package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.model.FintCustomerObjectEntityHandler;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateClientHandler extends FintCustomerObjectEntityHandler<Client, ClientEvent> {

    private final ClientService clientService;

    protected CreateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
        super(entityTopicService, entityProducerFactory, Client.class);
        this.clientService = clientService;
    }


    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.CREATE.name() + "-CLIENT";
    }

    @Override
    public void accept(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        Client client = clientService.addClient(consumerRecord.value().getObject(), organisation)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));

        clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
        clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());

        send(client);
    }
}
