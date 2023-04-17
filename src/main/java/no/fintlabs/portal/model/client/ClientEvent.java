package no.fintlabs.portal.model.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fintlabs.portal.model.FintCustomerObjectEvent;

@Getter
@AllArgsConstructor
public class ClientEvent extends FintCustomerObjectEvent<Client> {


    public ClientEvent(Client object, String orgId, Operation operation, String errorMessage) {
        super(object, orgId, operation, errorMessage);
    }

    public ClientEvent(Client object, String orgId, Operation operation) {
        super(object, orgId, operation, null);
    }
}
