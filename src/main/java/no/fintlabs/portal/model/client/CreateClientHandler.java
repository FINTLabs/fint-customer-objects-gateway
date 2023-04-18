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
public class CreateClientHandler extends FintCustomerObjectWithSecretsRequestHandler<Client, ClientEvent, ClientService> {


    private final ClientFactory clientFactory;

    private final ComponentService componentService;

    protected CreateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ClientCacheRepository clientCacheRepository, ClientFactory clientFactory, ComponentService componentService) {
        super(entityTopicService, entityProducerFactory, Client.class, clientCacheRepository, clientService);
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

        return getOrCreateClient(consumerRecord, organisation);
    }

    private Client getOrCreateClient(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        String clientDn = clientFactory.getClientDn(consumerRecord.value().getObject().getName(), organisation);
        Client client = consumerRecord.value().getObject();
        client.setDn(clientDn);

        return getFromCache(client)
                .map(this::handleFoundInCache)
                .orElseGet(() -> handleNotFoundInCache(consumerRecord, organisation));
    }

    private Client handleFoundInCache(Client client) {
        log.debug("Found client in cache {}", client.getDn());
        return client;
    }

    private Client handleNotFoundInCache(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.debug("Client {} not in cache", consumerRecord.value().getObject().getName());
        return objectService
                .getClientByName(
                        consumerRecord.value().getObject().getName(),
                        organisation)
                .map(client -> handleGetClientSuccess(consumerRecord, client))
                .orElseGet(() -> handleCreateClient(consumerRecord, organisation));
    }

    private Client handleCreateClient(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.debug("Client {} don't exist. Creating client...", consumerRecord.value().getObject().getName());
        Client client = objectService.addClient(consumerRecord.value().getObject(), organisation)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));

        ensureSecrets(consumerRecord, client);
        ensureComponents(consumerRecord);
        addToCache(client);

        return client;
    }

    private Client handleGetClientSuccess(ConsumerRecord<String, ClientEvent> consumerRecord, Client client) {
        log.debug("The client ({}) already exists", client.getDn());
        ensureSecrets(consumerRecord, client);
        addToCache(client);
        return client;
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
