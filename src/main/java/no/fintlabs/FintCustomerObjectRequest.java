package no.fintlabs;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public final class FintCustomerObjectRequest {
    private String orgId;
    private String objectId;
}
