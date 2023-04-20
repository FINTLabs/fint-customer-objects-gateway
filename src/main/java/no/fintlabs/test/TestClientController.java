package no.fintlabs.test;

import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientDBRepository;
import no.fintlabs.portal.model.client.ClientEvent;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("client")
public class TestClientController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;


    private final ClientDBRepository clientDBRepository;



    private final ClientEventRequestProducerService requestProducerService;

    public TestClientController(SecretService secretService,  ClientDBRepository clientDBRepository, ClientEventRequestProducerService requestProducerService) throws NoSuchAlgorithmException {
        this.secretService = secretService;
        this.clientDBRepository = clientDBRepository;
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

        clientEvent.setOperation(FintCustomerObjectEvent.Operation.CREATE);
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
    public ResponseEntity<ClientEvent> generateUpdateClientEvent(@RequestBody ClientEvent clientEvent) {

        clientEvent.setOperation(FintCustomerObjectEvent.Operation.UPDATE);
        clientEvent.getObject().setPublicKey(publicKey);
        return requestProducerService
                .get(clientEvent)
                .map(ce -> {
                    if (ce.hasError()) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ce);
                    }
                    return ResponseEntity.ok(ce);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @DeleteMapping()
    public ResponseEntity<ClientEvent> generateDeleteClientEvent(@RequestBody ClientEvent clientEvent) {

        clientEvent.setOperation(FintCustomerObjectEvent.Operation.DELETE);
        return requestProducerService
                .get(clientEvent)
                .map(ce -> {
                    if (ce.hasError()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ce);
                    }
                    return ResponseEntity.ok(ce);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PostMapping("decrypt")
    public ResponseEntity<Client> decryptClient(@RequestBody ClientEvent clientEvent) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        clientEvent.getObject().setClientSecret(secretService.decrypt(privateKey, clientEvent.getObject().getClientSecret()));
        clientEvent.getObject().setPassword(secretService.decrypt(privateKey, clientEvent.getObject().getPassword()));

        return ResponseEntity.ok(clientEvent.getObject());

    }

    @GetMapping("cache-size")
    public ResponseEntity<Long> cacheSize() {
        return ResponseEntity.ok(clientDBRepository.count());
    }

    @GetMapping("cache")
    public ResponseEntity<Collection<Client>> cache() {
        return ResponseEntity.ok(clientDBRepository.findAll());
    }
}
