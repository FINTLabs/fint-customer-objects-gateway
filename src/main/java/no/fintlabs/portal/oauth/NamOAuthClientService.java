package no.fintlabs.portal.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.portal.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class NamOAuthClientService {

    public static final int RETRY_ATTEMPTS = 10;
    public static final int RETRY_SLEEP_MS = 350;
    @Autowired
    private ObjectMapper mapper;
    @Value("${fint.nam.oauth.username}")
    private String username;
    @Value("${fint.nam.oauth.password}")
    private String password;
    @Value("${fint.nam.oauth.idp-hostname}")
    private String oauthIdpHostname;
    @Value("${fint.nam.idp-hostname}")
    private String idpHostname;
    @Value("${fint.nam.oauth.clientId}")
    private String clientId;
    @Value("${fint.nam.oauth.clientSecret}")
    private String clientSecret;

    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {

        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setUsername(username);
        resourceDetails.setPassword(password);
        resourceDetails.setAccessTokenUri(String.format(NamOAuthConstants.ACCESS_TOKEN_URL_TEMPLATE, oauthIdpHostname));
        resourceDetails.setClientId(clientId);
        resourceDetails.setClientSecret(clientSecret);
        resourceDetails.setGrantType(NamOAuthConstants.PASSWORD_GRANT_TYPE);
        resourceDetails.setScope(Collections.singletonList(NamOAuthConstants.SCOPE));

        restTemplate = new OAuth2RestTemplate(resourceDetails);
    }

    public OAuthClient addOAuthClient(String name) {
        log.info("Adding client {}...", name);
        HttpEntity<String> request;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            request = new HttpEntity<>(mapper.writeValueAsString(new OAuthClient(name)), headers);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize OAuth client request", e);
        }

        try {
            var url = String.format(NamOAuthConstants.CLIENT_REGISTRATION_URL_TEMPLATE, idpHostname);
            String response = restTemplate.postForObject(url, request, String.class);
            OAuthClient client = mapper.readValue(response, OAuthClient.class);
            log.info("Client ID {} created.", client.getClientId());
            return client;
        } catch (InvalidClientException e) {
            if (clientAlreadyExists(e)) {
                return getOAuthClientByName(name);
            }
            throw e;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize OAuth client creation response", e);
        } catch (Exception e) {
            log.error("Unable to create client {}", name, e);
            throw new RuntimeException(e);
        }
    }

    public void removeOAuthClient(String clientId) {
        log.info("Deleting client {}...", clientId);
        try {
            var url  = String.format(NamOAuthConstants.CLIENT_URL_TEMPLATE, idpHostname);
            restTemplate.delete(url, clientId);
        } catch (Exception e) {
            log.error("Unable to delete client {}", clientId, e);
            throw e;
        }
    }

    public OAuthClient getOAuthClient(String clientId) {
        log.info("Fetching client {}...", clientId);
        for (int i = 1; true; i++) {
            try {
                var url = String.format(NamOAuthConstants.CLIENT_URL_TEMPLATE, idpHostname);
                return restTemplate.getForObject(url, OAuthClient.class, clientId);
            } catch (Exception e) {
                log.warn("Unable to get client {}, this was iteration number {}", clientId, i);
                log.warn("Error, will retry: " + e.getMessage());

                if (i == RETRY_ATTEMPTS) {
                    log.error("Failed to getOauthClient after max retry attempts. Giving up", e);
                    throw e;
                }

                sleep(i);
            }
        }
    }

    public OAuthClient getOAuthClientByName(String name) {
        log.info("Fetching client by name {}...", name);

        try {
            var url = String.format(NamOAuthConstants.CLIENT_LIST_URL_TEMPLATE, idpHostname);
            String response = restTemplate.getForObject(url, String.class);
            List<OAuthClient> clients = mapper.readValue(response, new TypeReference<List<OAuthClient>>() {
            });

            return clients.stream()
                    .filter(client -> Objects.equals(name, client.getClientName()))
                    .findFirst()
                    .orElseThrow(() -> new ObjectNotFoundException(
                            String.format("OAuth client with name '%s' was not found", name)
                    ));
        } catch (Exception e) {
            log.error("Unable to get client by name {}", name, e);
            throw new RuntimeException(e);
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i * i * RETRY_SLEEP_MS);
        } catch (InterruptedException ex) {
            log.debug("Usually doesn't happen", ex);
        }
    }

    private boolean clientAlreadyExists(InvalidClientException e) {
        return e.getMessage() != null && e.getMessage().startsWith("The client already exists");
    }
}
