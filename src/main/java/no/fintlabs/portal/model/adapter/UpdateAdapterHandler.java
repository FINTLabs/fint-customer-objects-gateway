package no.fintlabs.portal.model.adapter;

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
public class UpdateAdapterHandler extends FintCustomerObjectWithSecretsHandler<Adapter, AdapterEvent, AdapterService> {

    private final ComponentService componentService;

    protected UpdateAdapterHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                                   AdapterService adapterService, ComponentService componentService){
        super(entityTopicService, entityProducerFactory, Adapter.class, adapterService);
        this.componentService = componentService;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.UPDATE.name() + "-ADAPTER";
    }

    @Override
    public Adapter apply(ConsumerRecord<String, AdapterEvent> consumerRecord, Organisation organisation) {
        
        Adapter desiredAdapter = consumerRecord.value().getObject();
        desiredAdapter.clearSecrets();

        Adapter currentAdapter = objectService.getAdapterByDn(desiredAdapter.getDn())
                .orElseThrow(() -> new RuntimeException("Unable to find adapter: "));

        ensureComponents(consumerRecord, currentAdapter);

        Adapter updatedAdapter = objectService.updateAdapter(desiredAdapter)
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while updating adapter"));
        send(updatedAdapter);

        return updatedAdapter;
    }

    private void ensureComponents(ConsumerRecord<String, AdapterEvent> consumerRecord, Adapter currentAdapter) {
        List<String> componentsToAdd = new ArrayList<>((CollectionUtils.removeAll(consumerRecord.value().getObject().getComponents(), currentAdapter.getComponents())));
        List<String> componentsToRemove = new ArrayList<>((CollectionUtils.removeAll(currentAdapter.getComponents(), consumerRecord.value().getObject().getComponents())));

        componentsToAdd.forEach(s -> {
            Component component = componentService.getComponentByDn(s)
                    .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
            componentService.linkAdapter(component, currentAdapter);
        });

        componentsToRemove.forEach(s -> {
            Component component = componentService.getComponentByDn(s)
                    .orElseThrow(() -> new RuntimeException("Unable to find component: " + s));
            componentService.unLinkAdapter(component, currentAdapter);
        });
    }


}
