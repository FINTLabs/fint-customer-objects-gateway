package no.fintlabs;

import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientEvent;
import no.fintlabs.portal.model.client.ClientService;
import no.fintlabs.portal.utilities.SecretService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.Base64;
import java.util.List;

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("/")
public class TestController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;

    private final ClientService clientService;

    public TestController(SecretService secretService, ClientService clientService) throws NoSuchAlgorithmException {
        this.secretService = secretService;
        this.clientService = clientService;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));

    }

    @PostMapping("client")
    public ResponseEntity<ClientEvent> generateCreateClientEvent() {
        Client client = new Client();
        client.setName("test-" + RandomStringUtils.randomAlphabetic(5));
        client.setShortDescription("Test");
        client.setPublicKey(publicKey);

        ClientEvent clientEvent = new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.CREATE);

        return ResponseEntity.ok(clientEvent);
    }

    @PutMapping("client")
    public ResponseEntity<ClientEvent> generateUpdateClientEvent() {
        Client client = clientService.getClients("fintlabs_no").stream().findAny().orElseThrow();
        client.setPublicKey(publicKey);

        ClientEvent clientEvent = new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.UPDATE);

        return ResponseEntity.ok(clientEvent);
    }

    @PostMapping("client/decrypt")
    public ResponseEntity<Client> decryptClient(@RequestBody Client client) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        client.setClientSecret(secretService.decrypt(privateKey, client.getClientSecret()));
        client.setPassword(secretService.decrypt(privateKey, client.getPassword()));

        return ResponseEntity.ok(client);

    }
}
