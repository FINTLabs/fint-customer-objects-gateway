package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeleteClientHandler extends FintCustomerObjectRequestHandler<Client, ClientEvent> {

    private final ClientService clientService;
    private final ClientCacheRepository clientCacheRepository;

    protected DeleteClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ClientCacheRepository clientCacheRepository) {
        super(entityTopicService, entityProducerFactory, Client.class);
        this.clientService = clientService;
        this.clientCacheRepository = clientCacheRepository;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.DELETE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{} event", consumerRecord.value().getOperationWithType());

        Client client = clientService.getClientByDn(consumerRecord.value().getObject().getDn())
                .flatMap(clientService::deleteClient)
                .orElseThrow(() ->
                        new RuntimeException("Unable to find client with dn: " + consumerRecord.value().getObject().getDn())
                );

        sendDelete(client.getDn());
        clientCacheRepository.remove(client);

        return client;
    }
}
