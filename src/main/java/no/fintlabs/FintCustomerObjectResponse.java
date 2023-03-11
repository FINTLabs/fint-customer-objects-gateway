package no.fintlabs;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public class FintCustomerObjectResponse<T> {

    private T customerObject;
    private boolean successful;
    private String message;
}
