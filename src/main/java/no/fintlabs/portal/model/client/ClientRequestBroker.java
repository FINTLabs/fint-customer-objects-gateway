package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.model.FintCustomerObjectRequestBroker;
import no.fintlabs.portal.model.FintCustomerObjectHandler;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class ClientRequestBroker extends FintCustomerObjectRequestBroker<ClientEvent, Client> {
    public ClientRequestBroker(OrganisationService organisationService, Collection<FintCustomerObjectHandler<Client, ClientEvent>> fintCustomerObjectHandlers) {
        super(organisationService, fintCustomerObjectHandlers);
    }
}
