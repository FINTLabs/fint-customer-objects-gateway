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
public class CreateAdapterHandler extends FintCustomerObjectEntityHandler<Adapter, AdapterEvent> {
    private final AdapterService adapterService;
    protected CreateAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory, AdapterService adapterService, AdapterService adapterService1) {
        super(entityTopicService, entityProducerFactory, Adapter.class);
        this.adapterService = adapterService1;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.CREATE.name() + "-ADAPTER";
    }

    @Override
    public void accept(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        log.info("{}", consumerRecord);
        log.info("{}", organisation);

        Adapter adapter = adapterService.addAdapter(consumerRecord.value().getObject(), organisation)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while creating adapter."));

        adapterService.resetAdapterPassword(adapter, consumerRecord.value().getObject().getPublicKey());
        adapterService.encryptClientSecret(adapter, consumerRecord.value().getObject().getPublicKey());

        send(adapter);
    }
}
