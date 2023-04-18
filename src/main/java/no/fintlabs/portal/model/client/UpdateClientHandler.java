package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsRequestHandler;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Component
public class UpdateClientHandler extends FintCustomerObjectWithSecretsRequestHandler<Client, ClientEvent> {
    private final ClientService clientService;

    private final ComponentService componentService;


    protected UpdateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ComponentService componentService, ClientCacheRepository clientCacheRepository) {
        super(entityTopicService, entityProducerFactory, Client.class, clientCacheRepository);
        this.clientService = clientService;
        this.componentService = componentService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.UPDATE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {

        log.info("{} event", consumerRecord.value().getOperationWithType());


        Client currentClient = clientService.getClientByDn(consumerRecord.value().getObject().getDn())
                .orElseThrow(() -> new RuntimeException("Unable to find client: " /*+ requestedClient.getDn() */));

        ensureComponents(consumerRecord, currentClient);


        Client updatedClient = clientService.updateClient(consumerRecord.value().getObject())
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while updating client."));

        clientService.resetClientPassword(updatedClient, consumerRecord.value().getObject().getPublicKey());
        clientService.encryptClientSecret(updatedClient, consumerRecord.value().getObject().getPublicKey());

        send(updatedClient);
        updateCache(updatedClient);

        return updatedClient;
    }

    private void ensureComponents(ConsumerRecord<String, ClientEvent> consumerRecord, Client currentClient) {
        List<String> componentsToAdd = new ArrayList<>((CollectionUtils.removeAll(consumerRecord.value().getObject().getComponents(), currentClient.getComponents())));
        List<String> componentsToRemove = new ArrayList<>((CollectionUtils.removeAll(currentClient.getComponents(), consumerRecord.value().getObject().getComponents())));

        componentsToAdd.forEach(s -> {
            Component component = componentService.getComponentByDn(s)
                    .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
            componentService.linkClient(component, consumerRecord.value().getObject());
        });

        componentsToRemove.forEach(s -> {
            Component component = componentService.getComponentByDn(s)
                    .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
            componentService.unLinkClient(component, consumerRecord.value().getObject());
        });
    }
}
