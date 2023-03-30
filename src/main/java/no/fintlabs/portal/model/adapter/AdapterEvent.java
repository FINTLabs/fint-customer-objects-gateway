package no.fintlabs.portal.model.adapter;

import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.FintCustomerObjectEvent;

import javax.validation.constraints.NotNull;

@Getter
@Jacksonized
public class AdapterEvent extends FintCustomerObjectEvent<Adapter> {

    public AdapterEvent(Adapter object, String orgId, @NotNull Operation operation) {
        super(object, orgId, operation);
    }
}
