package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.utilities.MetricService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeleteClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {
    private final MetricService metricService;

    protected DeleteClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                  ClientService clientService, MetricService metricService) {
        super(entityTopicService, entityProducerFactory, Client.class, clientService);
        this.metricService = metricService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.DELETE.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        Client client = objectService
                .getClientByDn(consumerRecord.value().getObject().getDn())
                .flatMap(objectService::deleteClient)
                .orElseThrow(() -> new RuntimeException("Unable to find client with dn: " + consumerRecord.value().getObject().getDn()));

        sendDelete(client.getDn());
        metricService.registerClientDelete();

        return client;
    }
}
