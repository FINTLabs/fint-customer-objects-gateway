package no.fintlabs.test;

import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.adapter.AdapterDBRepository;
import no.fintlabs.portal.model.adapter.AdapterEvent;
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
import java.util.Optional;

@ConditionalOnProperty(prefix = "fint.customer-object-gateway", name = "mode", havingValue = "test")
@RestController
@RequestMapping("adapter")
public class TestAdapterController {

    private final SecretService secretService;
    private final PrivateKey privateKey;
    private final String publicKey;


    private final AdapterDBRepository adapterDBRepository;

    private final AdapterEventRequestProducerService requestProducerService;

    public TestAdapterController(SecretService secretService, AdapterDBRepository adapterDBRepository, AdapterEventRequestProducerService requestProducerService) throws NoSuchAlgorithmException {
        this.secretService = secretService;
        this.adapterDBRepository = adapterDBRepository;
        this.requestProducerService = requestProducerService;

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = Base64.getEncoder().encodeToString((pair.getPublic().getEncoded()));

    }

    @GetMapping
    public ResponseEntity<AdapterEvent> generateGetClientEvent(@PathVariable String dn) {

        Adapter adapter = new Adapter();
        adapter.setDn(dn);
        adapter.setPublicKey(publicKey);

        return requestProducerService
                .get(new AdapterEvent(adapter, "fintlabs.no", FintCustomerObjectEvent.Operation.READ))
                .map(ae -> {
                    if (ae.hasError()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ae);
                    }
                    return ResponseEntity.ok(ae);
                })
                .orElse(ResponseEntity.internalServerError().build());

    }

    @PostMapping()
    public ResponseEntity<AdapterEvent> generateCreateClientEvent(@RequestBody AdapterEvent adapterEvent) {
        adapterEvent.setOperation(FintCustomerObjectEvent.Operation.CREATE);
        adapterEvent.getObject().setPublicKey(publicKey);
        Optional<AdapterEvent> adapterEventResponse = requestProducerService.get(adapterEvent);

        return adapterEventResponse
                .map(ae -> {
                    if (ae.hasError()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ae);
                    }
                    return ResponseEntity.ok(ae);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PutMapping()
    public ResponseEntity<AdapterEvent> generateUpdateClientEvent(@RequestBody AdapterEvent adapterEvent) {

        adapterEvent.setOperation(FintCustomerObjectEvent.Operation.UPDATE);
        adapterEvent.getObject().setPublicKey(publicKey);
        return requestProducerService
                .get(adapterEvent)
                .map(ae -> {
                    if (ae.hasError()) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ae);
                    }
                    return ResponseEntity.ok(ae);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @DeleteMapping()
    public ResponseEntity<AdapterEvent> generateDeleteClientEvent(@RequestBody AdapterEvent adapterEvent) {

        adapterEvent.setOperation(FintCustomerObjectEvent.Operation.DELETE);
        return requestProducerService
                .get(adapterEvent)
                .map(ae -> {
                    if (ae.hasError()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ae);
                    }
                    return ResponseEntity.ok(ae);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PostMapping("password/reset")
    public ResponseEntity<AdapterEvent> resetClientPasswordClientEvent(@RequestBody AdapterEvent adapterEvent) {

        adapterEvent.setOperation(FintCustomerObjectEvent.Operation.RESET_PASSWORD);
        return requestProducerService
                .get(adapterEvent)
                .map(ae -> {
                    if (ae.hasError()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ae);
                    }
                    return ResponseEntity.ok(ae);
                })
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PostMapping("decrypt")
    public ResponseEntity<Adapter> decryptClient(@RequestBody AdapterEvent adapterEvent) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        adapterEvent.getObject().setClientSecret(secretService.decrypt(privateKey, adapterEvent.getObject().getClientSecret()));
        adapterEvent.getObject().setPassword(secretService.decrypt(privateKey, adapterEvent.getObject().getPassword()));

        return ResponseEntity.ok(adapterEvent.getObject());
    }

    @GetMapping("cache-size")
    public ResponseEntity<Long> cacheSize() {
        return ResponseEntity.ok(adapterDBRepository.count());
    }

    @GetMapping("cache")
    public ResponseEntity<Collection<Adapter>> cache() {
        return ResponseEntity.ok(adapterDBRepository.findAll());
    }

}
