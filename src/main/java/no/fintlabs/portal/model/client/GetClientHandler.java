package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.exceptions.ObjectNotFoundException;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetClientHandler extends FintCustomerObjectRequestHandler<Client, ClientEvent> {

    private final ClientService clientService;

    protected GetClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
        super(entityTopicService, entityProducerFactory, Client.class);
        this.clientService = clientService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.READ.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        return clientService.getClientByDn(consumerRecord.value().getObject().getDn())
                .map(client -> {
                    clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
                    clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());
                    send(client);

                    return client;
                })
                .orElseThrow(() -> new RuntimeException("Unable to find client: " + consumerRecord.value().getObject().getDn()));

    }
}
