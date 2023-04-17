package no.fintlabs.test;

import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientEvent;
import no.fintlabs.portal.model.client.ClientService;
import no.fintlabs.portal.utilities.SecretService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.Base64;
import java.util.Optional;

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("client")
public class TestClientController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;

    private final ClientService clientService;

    private final ClientEventRequestProducerService requestProducerService;

    public TestClientController(SecretService secretService, ClientService clientService, ClientEventRequestProducerService requestProducerService) throws NoSuchAlgorithmException {
        this.secretService = secretService;
        this.clientService = clientService;
        this.requestProducerService = requestProducerService;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));

    }

    @GetMapping("{dn}")
    public ResponseEntity<ClientEvent> generateGetClientEvent(@PathVariable String dn) {


        Client client = new Client();
        client.setDn(dn);
        client.setPublicKey(publicKey);

        return requestProducerService
                .get(new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.READ))
                .map(ce -> {
                    if (ce.hasError()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ce);
                    }
                    return ResponseEntity.ok(ce);
                })
                .orElse(ResponseEntity.internalServerError().build());

    }

    @PostMapping()
    public ResponseEntity<ClientEvent> generateCreateClientEvent(@RequestBody ClientEvent clientEvent) {

        clientEvent.getObject().setPublicKey(publicKey);
        Optional<ClientEvent> clientEventResponse = requestProducerService.get(clientEvent);

        return clientEventResponse
                .map(ce -> {
                    if (ce.hasError()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ce);
                    }
                    return ResponseEntity.ok(ce);
                })
                .orElse(ResponseEntity.internalServerError().build());

    }

    @PutMapping()
    public ResponseEntity<ClientEvent> generateUpdateClientEvent() {
        Client client = clientService.getClients("fintlabs_no").stream().findAny().orElseThrow();
        client.setPublicKey(publicKey);

        ClientEvent clientEvent = new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.UPDATE);

        return ResponseEntity.ok(clientEvent);
    }

    @DeleteMapping()
    public ResponseEntity<ClientEvent> generateDeleteClientEvent() {
        Client client = clientService.getClients("fintlabs_no").stream().findAny().orElseThrow();
        client.setPublicKey(publicKey);

        ClientEvent clientEvent = new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.DELETE);

        return ResponseEntity.ok(clientEvent);
    }

    @PostMapping("decrypt")
    public ResponseEntity<Client> decryptClient(@RequestBody Client client) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        client.setClientSecret(secretService.decrypt(privateKey, client.getClientSecret()));
        client.setPassword(secretService.decrypt(privateKey, client.getPassword()));

        return ResponseEntity.ok(client);

    }


}
