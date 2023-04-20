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
public class DeleteClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {

    protected DeleteClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
        super(entityTopicService, entityProducerFactory, Client.class, /*clientCacheRepository, */clientService);
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.DELETE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{} event", consumerRecord.value().getOperationWithType());

        Client client = objectService.getClientByDn(consumerRecord.value().getObject().getDn())
                .flatMap(objectService::deleteClient)
                .orElseThrow(() ->
                        new RuntimeException("Unable to find client with dn: " + consumerRecord.value().getObject().getDn())
                );

        sendDelete(client.getDn());
        //removeFromCache(client);

        return client;
    }
}
