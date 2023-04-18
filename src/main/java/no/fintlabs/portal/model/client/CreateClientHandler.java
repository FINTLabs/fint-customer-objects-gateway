package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsRequestHandler;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@org.springframework.stereotype.Component
public class CreateClientHandler extends FintCustomerObjectWithSecretsRequestHandler<Client, ClientEvent> {

    private final ClientService clientService;

    private final ClientFactory clientFactory;

    private final ComponentService componentService;

    protected CreateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ClientCacheRepository clientCacheRepository, ClientFactory clientFactory, ComponentService componentService) {
        super(entityTopicService, entityProducerFactory, Client.class, clientCacheRepository);
        this.clientService = clientService;
        this.clientFactory = clientFactory;
        this.componentService = componentService;
    }


    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.CREATE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{} event", consumerRecord.value().getOperationWithType());

        String clientDn = clientFactory.getClientDn(consumerRecord.value().getObject().getName(), organisation);
        Client object = consumerRecord.value().getObject();
        object.setDn(clientDn);

        return getFromCache(object)
                .map(client -> {
                    log.debug("Found client in cache {}", client.getDn());
                    return client;
                })
                .orElseGet(() -> {
                    log.debug("Client {} not in cache", consumerRecord.value().getObject().getName());
                    return clientService
                            .getClientByName(
                                    consumerRecord.value().getObject().getName(),
                                    organisation)
                            .map(client -> {
                                log.debug("The client ({}) already exists", client.getDn());
                                client.setPublicKey(consumerRecord.value().getObject().getPublicKey());
                                clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
                                clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());
                                addToCache(client);
                                return client;
                            })
                            .orElseGet(() -> {
                                log.debug("Client {} don't exist. Creating client...", consumerRecord.value().getObject().getName());
                                Client client = clientService.addClient(consumerRecord.value().getObject(), organisation)
                                        .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));

                                client.setPublicKey(consumerRecord.value().getObject().getPublicKey());
                                clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
                                clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());
                                addToCache(client);

                                return client;
                            });




                });
    }

    private void ensureComponents(ConsumerRecord<String, ClientEvent> consumerRecord) {
        consumerRecord.value().getObject().getComponents()
                .forEach(s -> {
                    Component component = componentService.getComponentByDn(s)
                            .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
                    componentService.linkClient(component, consumerRecord.value().getObject());
                });
    }
}
