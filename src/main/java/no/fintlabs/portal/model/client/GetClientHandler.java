package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsRequestHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetClientHandler extends FintCustomerObjectWithSecretsRequestHandler<Client, ClientEvent> {

    private final ClientService clientService;

    protected GetClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ClientCacheRepository clientCacheRepository) {
        super(entityTopicService, entityProducerFactory, Client.class, clientCacheRepository);
        this.clientService = clientService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.READ.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{} event.", consumerRecord.value().getOperationWithType());

        return getFromCache(consumerRecord.value().getObject())
                .map(client -> {
                    log.debug("Found client in cache {}", client.getDn());
                    return client;
                })
                .orElseGet(() ->
                        clientService.getClientByDn(consumerRecord.value().getObject().getDn())
                                .map(client -> {
                                    log.debug("Cound not find client ({}) in cache. Getting the client for LDAP", client.getDn());
                                    clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
                                    clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());
                                    send(client);
                                    client.setPublicKey(consumerRecord.value().getObject().getPublicKey());
                                    addToCache(client);

                                    return client;
                                })
                                .orElseThrow(() -> new RuntimeException("Unable to find client: " + consumerRecord.value().getObject().getDn()))
                );
    }
}
