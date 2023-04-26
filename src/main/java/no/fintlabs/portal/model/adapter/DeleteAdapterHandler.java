package no.fintlabs.portal.model.adapter;

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
public class DeleteAdapterHandler extends FintCustomerObjectWithSecretsHandler<Adapter, AdapterEvent, AdapterService> {

    protected DeleteAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                   AdapterService adapterService) {
        super(entityTopicService, entityProducerFactory, Adapter.class, adapterService);
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.DELETE.name() + "-ADAPTER";
    }

    @Override
    public Adapter apply(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        Adapter adapter = objectService
                .getAdapterByDn(consumerRecord.value().getObject().getDn())
                .flatMap(objectService::deleteAdapter)
                .orElseThrow(() -> new RuntimeException("Unable to find adapter with dn: " + consumerRecord.value().getObject().getDn()));

        sendDelete(adapter.getDn());

        return adapter;
    }
}
