package no.fintlabs;

import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.adapter.AdapterService;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientDto;
import no.fintlabs.portal.model.client.ClientService;
import no.fintlabs.portal.model.client.producers.ClientCreateRequestReplyProducerService;
import no.fintlabs.portal.model.client.producers.ClientDeleteRequestReplyProducerService;
import no.fintlabs.portal.model.client.producers.ClientGetRequestReplyProducerService;
import no.fintlabs.portal.model.client.producers.ClientUpdateRequestReplyProducerService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("test")
public class TestController {

    private final OrganisationService organisationService;

    private final AdapterService adapterService;

    private final ClientCreateRequestReplyProducerService clientCreateRequestReplyProducerService;
    private final ClientDeleteRequestReplyProducerService clientDeleteRequestReplyProducerService;
    private final ClientGetRequestReplyProducerService clientGetRequestReplyProducerService;
    private final ClientUpdateRequestReplyProducerService clientUpdateRequestReplyProducerService;

    private final ClientService clientService;

    public TestController(OrganisationService organisationService, AdapterService adapterService, ClientCreateRequestReplyProducerService clientCreateRequestReplyProducerService, ClientDeleteRequestReplyProducerService clientDeleteRequestReplyProducerService, ClientDeleteRequestReplyProducerService clientGetRequestReplyProducerService, ClientGetRequestReplyProducerService clientGetRequestReplyProducerService1, ClientGetRequestReplyProducerService clientGetRequestReplyProducerService2, ClientUpdateRequestReplyProducerService clientUpdateRequestReplyProducerService, ClientService clientService) {
        this.organisationService = organisationService;
        this.adapterService = adapterService;
        this.clientCreateRequestReplyProducerService = clientCreateRequestReplyProducerService;
        this.clientDeleteRequestReplyProducerService = clientDeleteRequestReplyProducerService;
        this.clientGetRequestReplyProducerService = clientGetRequestReplyProducerService2;
        this.clientUpdateRequestReplyProducerService = clientUpdateRequestReplyProducerService;
        this.clientService = clientService;
    }

    // {name: "test.no", orgNumber: "123456789", displayName: "Test"}
    @GetMapping("organisation")
    public ResponseEntity<Void> createOrganisation() {
        Organisation organisation = new Organisation();

        organisation.setName("egil.no");
        organisation.setOrgNumber("123456789");
        organisation.setDisplayName("The Egil Company");
        organisationService.createOrganisation(organisation);

        return ResponseEntity.ok().build();
    }

    // TODO: add client endpoints
    // { "name": "egil","note": "test-klient","shortDescription": "Egil Ballestad"}
    @PostMapping("clients")
    public ResponseEntity<Client> createClient(
            @RequestBody @Valid final ClientDto clientDto
    ) {
        return ResponseEntity.ok(clientCreateRequestReplyProducerService.get(clientDto).orElseThrow());
    }

    @DeleteMapping("clients")
    public ResponseEntity<ClientDto> deleteClient(
            @RequestBody @Valid final ClientDto clientDto
    ) {
        return ResponseEntity.ok(clientDeleteRequestReplyProducerService.get(clientDto).orElseThrow());
    }

    @GetMapping("clients")
    public ResponseEntity<Client> getClient(
            @RequestBody @Valid final ClientDto clientDto
    ) {
        return ResponseEntity.ok(clientGetRequestReplyProducerService.get(clientDto).orElseThrow());
    }

    @GetMapping("clients/{orgName}")
    public ResponseEntity<List<Client>> getAllClients(@PathVariable("orgName") final String orgName) {
        List<Client> list = clientService.getClients(orgName);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(list);
    }

//    {"name": "egil@client.fintlabs.no","note": "test-klient updated","shortDescription": "Egil Ballestad"}
    @PutMapping("clients")
    public ResponseEntity<Client> updateClient(
        @RequestBody @Valid final ClientDto clientDto
    ){
        return ResponseEntity.ok(clientUpdateRequestReplyProducerService.get(clientDto).orElseThrow());
    }

    @PostMapping("adapter/{orgName}")
    public ResponseEntity<Adapter> addAdapter(@PathVariable("orgName") final String orgName,
                                              @RequestBody final Adapter adapter) {

        Optional<Organisation> organisation = organisationService.getOrganisation(orgName);

        Optional<Adapter> optionalAdapter = adapterService.getAdapter(adapter.getName(), orgName);
        if (!optionalAdapter.isPresent()) {
            if (adapterService.addAdapter(adapter, organisation.get())) {

                return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(adapter);
            }
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("adapter/{orgName}/{adapterName}")
    public ResponseEntity<Void> deleteAdapter(@PathVariable("orgName") final String orgName,
                                              @PathVariable final String adapterName) {
        Optional<Organisation> organisation = organisationService.getOrganisation(orgName);
        Optional<Adapter> adapter = adapterService.getAdapter(adapterName, organisation.get().getName());

        adapterService.deleteAdapter(adapter.get());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }


}
