package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsHandler;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@org.springframework.stereotype.Component
public class CreateClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {


    private final ClientFactory clientFactory;

    private final ComponentService componentService;


    protected CreateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                  ClientService clientService, ClientFactory clientFactory,
                                  ComponentService componentService) {
        super(entityTopicService, entityProducerFactory, Client.class, clientService);
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

        return objectService.getClientByDn(clientDn)
                .orElseGet(() -> objectService.addClient(client, organisation)
                        .map(this::ensureComponents)
                        .orElseThrow());

    }

    private Client ensureComponents(Client client) {
        client.getComponents()
                .forEach(s -> {
                    Component component = componentService.getComponentByDn(s)
                            .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
                    componentService.linkClient(component, client);
                });

        return client;
    }
}
