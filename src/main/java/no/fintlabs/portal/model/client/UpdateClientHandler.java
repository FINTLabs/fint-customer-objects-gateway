package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsHandler;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Component
public class UpdateClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {

    private final ComponentService componentService;


    protected UpdateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ComponentService componentService, ClientCacheRepository clientCacheRepository) {
        super(entityTopicService, entityProducerFactory, Client.class, clientCacheRepository, clientService);
        this.componentService = componentService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.UPDATE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {

        log.info("{} event", consumerRecord.value().getOperationWithType());

        Client desiredClient = consumerRecord.value().getObject();
        desiredClient.clearSecrets();

        Client currentClient = objectService.getClientByDn(desiredClient.getDn())
                .orElseThrow(() -> new RuntimeException("Unable to find client: "));

        ensureComponents(consumerRecord, currentClient);


        Client updatedClient = objectService.updateClient(desiredClient)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while updating client."));

        ensureSecrets(consumerRecord, updatedClient);
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
            componentService.linkClient(component, currentClient);
        });

        componentsToRemove.forEach(s -> {
            Component component = componentService.getComponentByDn(s)
                    .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
            componentService.unLinkClient(component, currentClient);
        });
    }
}
