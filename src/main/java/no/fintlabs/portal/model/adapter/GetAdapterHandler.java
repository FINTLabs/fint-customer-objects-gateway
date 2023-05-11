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
public class GetAdapterHandler extends FintCustomerObjectWithSecretsHandler<Adapter, AdapterEvent, AdapterService> {
    private final AdapterService adapterService;

    protected GetAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                AdapterService adapterService, AdapterService adapterService1) {
        super(entityTopicService, entityProducerFactory, Adapter.class, adapterService);
        this.adapterService = adapterService1;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.READ.name() + "-ADAPTER";
    }

    @Override
    public Adapter apply(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        return adapterService.getAdapterByDn(consumerRecord.value().getObject().getDn())
                .orElseThrow(() -> new RuntimeException("Unable to find adapter: " + consumerRecord.value().getObject().getDn()));
    }
}
