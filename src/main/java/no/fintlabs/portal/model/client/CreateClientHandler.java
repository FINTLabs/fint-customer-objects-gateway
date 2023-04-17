package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@org.springframework.stereotype.Component
public class CreateClientHandler extends FintCustomerObjectRequestHandler<Client, ClientEvent> {

    private final ClientService clientService;

    private final ComponentService componentService;

    protected CreateClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, ClientService clientService, ComponentService componentService) {
        super(entityTopicService, entityProducerFactory, Client.class);
        this.clientService = clientService;
        this.componentService = componentService;
    }


    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.CREATE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        Client client = clientService.addClient(consumerRecord.value().getObject(), organisation)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating client."));

        ensureComponents(consumerRecord);

        clientService.resetClientPassword(client, consumerRecord.value().getObject().getPublicKey());
        clientService.encryptClientSecret(client, consumerRecord.value().getObject().getPublicKey());

        send(client);

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
