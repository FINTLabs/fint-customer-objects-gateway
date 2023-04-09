package no.fintlabs;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientEvent;
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

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("/")
public class TestController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;

    public TestController(SecretService secretService) throws NoSuchAlgorithmException {
        this.secretService = secretService;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));

    }

    @GetMapping("client")
    public ResponseEntity<ClientEvent> generateClientEvent() throws JsonProcessingException {
        Client client = new Client();
        client.setName("test-" + RandomStringUtils.randomAlphabetic(5));
        client.setShortDescription("Test");
        client.setPublicKey(publicKey);

        ClientEvent clientEvent = new ClientEvent(client, "fintlabs.no", FintCustomerObjectEvent.Operation.CREATE);

        clientEvent.setOperation(FintCustomerObjectEvent.Operation.CREATE);

        return ResponseEntity.ok(clientEvent);
    }

    @PostMapping("client")
    public ResponseEntity<Client> decryptClient(@RequestBody Client client) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        client.setClientSecret(secretService.decrypt(privateKey, client.getClientSecret()));
        client.setPassword(secretService.decrypt(privateKey, client.getPassword()));

        return ResponseEntity.ok(client);

    }
}
