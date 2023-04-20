package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {

    private final ClientService clientService;

    protected PasswordClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                    ClientService clientService, ClientService clientService1) {
        super(entityTopicService, entityProducerFactory, Client.class, clientService);
        this.clientService = clientService1;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.RESET_PASSWORD.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        return clientService.getClientByDn(consumerRecord.value().getObject().getDn())
                .map(client -> {
                    clientService.resetAndEncryptPassword(client, client.getPublicKey());
                    return client;
                })
                .orElseThrow(() -> new RuntimeException("Unable to find client: " + consumerRecord.value().getObject().getDn()));
    }
}
