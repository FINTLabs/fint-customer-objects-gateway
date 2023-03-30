package no.fintlabs.portal.model.adapter;

import no.fintlabs.FintCustomerObjectEntityHandler;
import no.fintlabs.FintCustomerObjectEventHandler;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AdapterEventHandler extends FintCustomerObjectEventHandler<AdapterEvent, Adapter> {
    public AdapterEventHandler(EventTopicService eventTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, Collection<FintCustomerObjectEntityHandler<Adapter, AdapterEvent>> fintCustomerObjectEntityHandlers) {
        super(eventTopicService, consumer, organisationService, fintCustomerObjectEntityHandlers, Adapter.class);
    }
}
