package no.fintlabs.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.fintlabs.portal.ldap.BasicLdapEntry;

import java.lang.reflect.ParameterizedType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FintCustomerObjectEvent<T extends BasicLdapEntry> {
    private T object;
    private String orgId;
    private Operation operation;

    @JsonIgnore
    public String getOrganisationObjectName() {
        return orgId.replaceAll("\\.", "_");
    }

    @JsonIgnore
    public String getOperationWithType() {
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
}
