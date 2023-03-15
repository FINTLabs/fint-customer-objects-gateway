package no.fintlabs.portal.model.client;

import no.fintlabs.FintCustomerObjectEntityHandler;
import no.fintlabs.FintCustomerObjectEventHandler;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ClientHandler extends FintCustomerObjectEventHandler<ClientEvent, Client> {


    public ClientHandler(EventTopicService eventTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, Collection<FintCustomerObjectEntityHandler<Client, ClientEvent>> fintCustomerObjectEntityHandlers) {
        super(eventTopicService, consumer, organisationService, fintCustomerObjectEntityHandlers, Client.class);
    }
}
