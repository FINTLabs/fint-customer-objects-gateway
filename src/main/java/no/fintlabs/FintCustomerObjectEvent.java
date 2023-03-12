package no.fintlabs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;


@Getter
@AllArgsConstructor
public abstract class FintCustomerObjectEvent<T> {
    private T object;
    private String orgId;
    private Status status;

    @JsonIgnore
    public String getOrganisationObjectName() {
        return orgId.replaceAll("\\.", "_");
    }



    @Getter
    @Builder
    @Jacksonized
    public static final class Status {
        private boolean successful;
        private String message;
    }
}
