package no.fintlabs.portal.model.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fintlabs.portal.model.FintCustomerObjectEvent;

@Getter
@AllArgsConstructor
public class AdapterEvent extends FintCustomerObjectEvent<Adapter> {

    public AdapterEvent(Adapter object, String orgId, Operation operation) {
        super(object, orgId, operation);
    }
}
