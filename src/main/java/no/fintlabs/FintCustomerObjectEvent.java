package no.fintlabs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import no.fintlabs.portal.ldap.BasicLdapEntry;

import javax.validation.constraints.NotNull;
import java.lang.reflect.ParameterizedType;


@Getter
@Setter
@AllArgsConstructor
public abstract class FintCustomerObjectEvent<T extends BasicLdapEntry> {
    private T object;
    private String orgId;
    @NotNull
    private Operation operation;
    //private Status status;

    @JsonIgnore
    public String getOrganisationObjectName() {
        return orgId.replaceAll("\\.", "_");
    }

    public String getOperation() {
        return String.format("%s-%s", operation.name(), getParameterClass().getSimpleName().toUpperCase());
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterClass() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public enum Operation {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

//    @Getter
//    @Builder
//    @Jacksonized
//    public static final class Status {
//        private boolean successful;
//        private String message;
//    }
}
