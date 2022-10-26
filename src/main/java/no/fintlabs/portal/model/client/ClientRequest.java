package no.fintlabs.portal.model.client;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;

@Data
public class ClientRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String note;
    @NotBlank
    private String shortDescription;
    @NotBlank
    private String orgName;

    private List<String> components = Collections.emptyList();
}
