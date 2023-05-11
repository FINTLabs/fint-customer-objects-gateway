package no.fintlabs.portal.model.adapter;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.portal.model.FintCustomerObjectHandler;
import no.fintlabs.portal.model.FintCustomerObjectRequestBroker;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class AdapterRequestBroker extends FintCustomerObjectRequestBroker<AdapterEvent, Adapter> {
    public AdapterRequestBroker(OrganisationService organisationService, Collection<FintCustomerObjectHandler<Adapter, AdapterEvent>> fintCustomerObjectHandlers,RequestConsumerFactoryService requestConsumerFactoryService,
                                RequestTopicService requestTopicService) {
        super(organisationService, fintCustomerObjectHandlers, requestConsumerFactoryService, requestTopicService);
    }
}
