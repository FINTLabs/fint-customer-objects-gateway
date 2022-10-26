package no.fintlabs;

import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientRequest;
import no.fintlabs.portal.model.client.ClientService;
import no.fintlabs.portal.model.client.producers.ClientCreateRequestReplyProducerService;
import no.fintlabs.portal.model.client.producers.ClientDeleteRequestReplyProducerService;
import no.fintlabs.portal.model.client.producers.ClientGetRequestReplyProducerService;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("test/clients")
public class TestClientController {

    private final ClientCreateRequestReplyProducerService clientCreateRequestReplyProducerService;
    private final ClientDeleteRequestReplyProducerService clientDeleteRequestReplyProducerService;
    private final ClientGetRequestReplyProducerService clientGetRequestReplyProducerService;
    private final ClientService clientService;
    private final ComponentService componentService;
    private final OrganisationService organisationService;

    public TestClientController(ClientCreateRequestReplyProducerService clientCreateRequestReplyProducerService, ClientDeleteRequestReplyProducerService clientDeleteRequestReplyProducerService, ClientDeleteRequestReplyProducerService clientGetRequestReplyProducerService, ClientGetRequestReplyProducerService clientGetRequestReplyProducerService1, ClientGetRequestReplyProducerService clientGetRequestReplyProducerService2, ClientService clientService, ComponentService componentService, OrganisationService organisationService) {
        this.clientCreateRequestReplyProducerService = clientCreateRequestReplyProducerService;
        this.clientDeleteRequestReplyProducerService = clientDeleteRequestReplyProducerService;
        this.clientGetRequestReplyProducerService = clientGetRequestReplyProducerService2;
        this.clientService = clientService;
        this.componentService = componentService;
        this.organisationService = organisationService;
    }

    // { "name": "egil","note": "test-klient","shortDescription": "Egil Ballestad"}
    @PostMapping
    public ResponseEntity<Client> createClient(
            @RequestBody final ClientRequest clientRequest
    ) {
        return ResponseEntity.ok(clientCreateRequestReplyProducerService.get(clientRequest).orElseThrow());
    }

    @DeleteMapping
    public ResponseEntity<ClientRequest> deleteClient(
            @RequestBody final ClientRequest clientRequest
    ) {
        return ResponseEntity.ok(clientDeleteRequestReplyProducerService.get(clientRequest).orElseThrow());
    }

    @GetMapping
    public ResponseEntity<Client> getClient(
            @RequestBody final ClientRequest clientRequest
    ) {
        return ResponseEntity.ok(clientGetRequestReplyProducerService.get(clientRequest).orElseThrow());
    }

}
