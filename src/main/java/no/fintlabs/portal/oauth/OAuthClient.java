package no.fintlabs.portal.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthClient {

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("application_type")
    private String applicationType;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("corsdomains")
    private List<String> corsDomains;

    public OAuthClient(String name) {
        clientName = name;

        grantTypes = NamOAuthConstants.GRANT_TYPE;
        applicationType = NamOAuthConstants.APPLICATION_TYPE;
        redirectUris = Collections.singletonList(NamOAuthConstants.DEFAULT_REDIRECT_URI);
        responseTypes = Collections.singletonList(NamOAuthConstants.RESPONSE_TYPE);
        corsDomains = NamOAuthConstants.CORS_DOMAINS;
    }
}
