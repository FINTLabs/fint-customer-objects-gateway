package no.fintlabs.portal.model.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientReply {
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String orgId;
    private String errorMessage;
    @Builder.Default
    private boolean successful = true;
}
