package no.fintlabs.test;

import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.adapter.AdapterEvent;
import no.fintlabs.portal.model.adapter.AdapterService;
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

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("adapter")
public class TestAdapterController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;

    private final AdapterService adapterService;

    public TestAdapterController(SecretService secretService, AdapterService adapterService) throws NoSuchAlgorithmException {
        this.secretService = secretService;
        this.adapterService = adapterService;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));

    }

    @GetMapping
    public ResponseEntity<AdapterEvent> generateGetClientEvent() {
        Adapter adapter = adapterService.getAdapters("fintlabs_no").stream().findAny().orElseThrow();
        adapter.setPublicKey(publicKey);
        // These properties are set to null so that we only get the dn and public key in response. This is the only
        // properties we need for a read, but we can have them all if we like.
        adapter.setClientId(null);
        adapter.setShortDescription(null);
        adapter.setNote(null);
        adapter.setName(null);

        AdapterEvent adapterEvent = new AdapterEvent(adapter, "fintlabs.no", FintCustomerObjectEvent.Operation.READ);

        return ResponseEntity.ok(adapterEvent);
    }

    @PostMapping()
    public ResponseEntity<AdapterEvent> generateCreateClientEvent() {
        Adapter adapter = new Adapter();
        adapter.setName("test-" + RandomStringUtils.randomAlphabetic(5));
        adapter.setShortDescription("Test");
        adapter.setPublicKey(publicKey);

        AdapterEvent clientEvent = new AdapterEvent(adapter, "fintlabs.no", FintCustomerObjectEvent.Operation.CREATE);

        return ResponseEntity.ok(clientEvent);
    }

    @PutMapping()
    public ResponseEntity<AdapterEvent> generateUpdateClientEvent() {
        Adapter adapter = adapterService.getAdapters("fintlabs_no").stream().findAny().orElseThrow();
        adapter.setPublicKey(publicKey);

        AdapterEvent adapterEvent = new AdapterEvent(adapter, "fintlabs.no", FintCustomerObjectEvent.Operation.UPDATE);

        return ResponseEntity.ok(adapterEvent);
    }

    @DeleteMapping()
    public ResponseEntity<AdapterEvent> generateDeleteClientEvent() {
        Adapter adapter = adapterService.getAdapters("fintlabs_no").stream().findAny().orElseThrow();
        adapter.setPublicKey(publicKey);

        AdapterEvent adapterEvent = new AdapterEvent(adapter, "fintlabs.no", FintCustomerObjectEvent.Operation.DELETE);

        return ResponseEntity.ok(adapterEvent);
    }

    @PostMapping("decrypt")
    public ResponseEntity<Adapter> decryptClient(@RequestBody Adapter adapter) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        adapter.setClientSecret(secretService.decrypt(privateKey, adapter.getClientSecret()));
        adapter.setPassword(secretService.decrypt(privateKey, adapter.getPassword()));

        return ResponseEntity.ok(adapter);

    }
}
