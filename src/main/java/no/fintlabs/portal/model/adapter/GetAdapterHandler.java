package no.fintlabs.portal.model.adapter;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.model.FintCustomerObjectEntityHandler;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetAdapterHandler extends FintCustomerObjectEntityHandler<Adapter, AdapterEvent> {
    private final AdapterService adapterService;

    protected GetAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, AdapterService adapterService) {
        super(entityTopicService, entityProducerFactory, Adapter.class);
        this.adapterService = adapterService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.READ.name() + "-ADAPTER";
    }

    @Override
    public void accept(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        Adapter adapter = adapterService.getAdapterByDn(consumerRecord.value().getObject().getDn())
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while reading adapter."));

        adapterService.resetAdapterPassword(adapter, consumerRecord.value().getObject().getPublicKey());
        adapterService.encryptClientSecret(adapter, consumerRecord.value().getObject().getPublicKey());
        send(adapter);
    }
}
