package no.fintlabs.portal.model.client;

import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.FintCustomerObjectEvent;

import javax.validation.constraints.NotNull;

//@Builder
@Getter
@Jacksonized
public class ClientEvent extends FintCustomerObjectEvent<Client> {


    public ClientEvent(Client object, String orgId, @NotNull Operation operation) {
        super(object, orgId, operation);
    }
}
