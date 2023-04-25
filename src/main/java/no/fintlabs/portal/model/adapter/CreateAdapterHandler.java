package no.fintlabs.portal.model.adapter;

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
public class CreateAdapterHandler extends FintCustomerObjectWithSecretsHandler<Adapter, AdapterEvent, AdapterService> {

    private final AdapterFactory adapterFactory;
    private final ComponentService componentService;
    protected CreateAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                   AdapterService adapterService, AdapterFactory adapterFactory,
                                   ComponentService componentService) {
        super(entityTopicService, entityProducerFactory, Adapter.class, adapterService);
        this.adapterFactory = adapterFactory;
        this.componentService = componentService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.CREATE.name() + "-ADAPTER";
    }

    @Override
    public Adapter apply(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        Adapter adapter = getOrCreateAdapter(consumerRecord, organisation);
        send(adapter);
        return adapter;
    }

    private Adapter getOrCreateAdapter(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        String adapterDn = adapterFactory.getAdapterDn(consumerRecord.value().getObject().getName(), organisation);
        Adapter adapter = consumerRecord.value().getObject();

        return objectService.getAdapterByDn(adapterDn)
                .orElseGet(() -> objectService.addAdapter(adapter, organisation)
                        .map(this::ensureComponents)
                        .orElseThrow());
    }

    private Adapter ensureComponents(Adapter adapter) {
        adapter.getComponents()
                .forEach(s -> {
                    Component component = componentService.getComponentByDn(s)
                            .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
                    componentService.linkAdapter(component, adapter);
                });

        return adapter;
    }
}
