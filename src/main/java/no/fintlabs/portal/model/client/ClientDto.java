package no.fintlabs.portal.model.client;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClientDto {
    @NotBlank
    private String name;
    @NotBlank
    private String note;
    @NotBlank
    private String shortDescription;
    @NotBlank
    private String orgName;
}
