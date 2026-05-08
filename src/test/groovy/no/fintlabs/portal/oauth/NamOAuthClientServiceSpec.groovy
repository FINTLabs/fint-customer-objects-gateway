package no.fintlabs.portal.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.portal.exceptions.ObjectNotFoundException
import org.springframework.http.HttpEntity
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.common.exceptions.InvalidClientException
import spock.lang.Specification

class NamOAuthClientServiceSpec extends Specification {

    private restTemplate
    private namOAuthClientService
    private mapper

    void setup() {
        restTemplate = Mock(OAuth2RestTemplate)
        mapper = new ObjectMapper()
        namOAuthClientService = new NamOAuthClientService(restTemplate: restTemplate, mapper: mapper)
    }

    def "Add OAuth Client"() {

        when:
        def client = namOAuthClientService.addOAuthClient("name")

        then:
        1 * restTemplate.postForObject(_ as String, _ as HttpEntity, _ as Class, _) >> "{\"developerDn\":\"dev\",\"grant_types\":[\"password\"],\"application_type\":\"web\",\"Version\":\"4.1\",\"client_secret_expires_at\":1506509030813,\"registration_client_uri\":\"https://idp/nidp/oauth/nam/clients//9f30fa40-0178-4cbe-8cf5-e27c18a3ecbd\",\"redirect_uris\":[\"https://dummy.com\"],\"client_secret\":\"thesecret\",\"client_id_issued_at\":1506422630813,\"client_name\":\"80c66be1-a24a-4b55-84ab-8faeb775a85b\",\"client_id\":\"theid\",\"response_types\":[\"token\"]}"
        client != null
        !client.getClientId().isEmpty()
        !client.getClientSecret().isEmpty()
    }

    def "Get OAuth Client By Name"() {

        when:
        def client = namOAuthClientService.getOAuthClientByName("target-client")

        then:
        1 * restTemplate.getForObject(_ as String, String.class) >> """[
            {"client_name":"other-client","client_id":"other-id","client_secret":"other-secret"},
            {"client_name":"target-client","client_id":"target-id","client_secret":"target-secret"}
        ]"""
        client != null
        client.getClientName() == "target-client"
        client.getClientId() == "target-id"
        client.getClientSecret() == "target-secret"
    }

    def "Get OAuth Client By Name throws when client is missing"() {

        when:
        namOAuthClientService.getOAuthClientByName("missing-client")

        then:
        1 * restTemplate.getForObject(_ as String, String.class) >> """[
            {"client_name":"other-client","client_id":"other-id","client_secret":"other-secret"}
        ]"""
        def e = thrown(ObjectNotFoundException)
        e.message == "OAuth client with name 'missing-client' was not found"
    }

    def "Add OAuth Client returns existing client when NAM says it already exists"() {

        when:
        def client = namOAuthClientService.addOAuthClient("existing-client")

        then:
        1 * restTemplate.postForObject(_ as String, _ as HttpEntity, _ as Class) >> {
            throw new InvalidClientException("The client already exists")
        }
        1 * restTemplate.getForObject(_ as String, String.class) >> """[
            {"client_name":"existing-client","client_id":"existing-id","client_secret":"existing-secret"}
        ]"""
        client != null
        client.getClientName() == "existing-client"
        client.getClientId() == "existing-id"
        client.getClientSecret() == "existing-secret"
    }
}
