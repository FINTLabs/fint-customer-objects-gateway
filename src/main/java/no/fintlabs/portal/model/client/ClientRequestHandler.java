package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.model.FintCustomerObjectRequestEventHandler;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class ClientRequestHandler extends FintCustomerObjectRequestEventHandler<ClientEvent, Client> {
    public ClientRequestHandler(OrganisationService organisationService, Collection<FintCustomerObjectRequestHandler<Client, ClientEvent>> fintCustomerObjectRequestHandlers) {
        super(organisationService, fintCustomerObjectRequestHandlers);
    }
}
