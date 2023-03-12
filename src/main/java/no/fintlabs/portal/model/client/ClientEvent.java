package no.fintlabs.portal.model.client;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.FintCustomerObjectEvent;

//@Builder
@Getter
@Jacksonized
public class ClientEvent extends FintCustomerObjectEvent<Client> {


    @Builder
    public ClientEvent(Client object, String orgId, Status status) {
        super(object, orgId, status);
    }
}
