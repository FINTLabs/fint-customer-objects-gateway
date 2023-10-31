package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.portal.model.FintCustomerObjectEvent;
import no.fintlabs.portal.model.FintCustomerObjectWithSecretsHandler;
import no.fintlabs.portal.model.organisation.Organisation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
public class GetClientHandler extends FintCustomerObjectWithSecretsHandler<Client, ClientEvent, ClientService> {

    private final ClientService clientService;

    protected GetClientHandler(EntityTopicService entityTopicService, EntityProducerFactory entityProducerFactory,
                               ClientService clientService, ClientService clientService1) {
        super(entityTopicService, entityProducerFactory, Client.class, clientService);
        this.clientService = clientService1;
    }

    @Override
    public String operation() {
        return FintCustomerObjectEvent.Operation.READ.name() + "-CLIENT";
    }

    @Override
    public Client apply(ConsumerRecord<String, ClientEvent> consumerRecord, Organisation organisation) {
        Client request = consumerRecord.value().getObject();
        Optional<Client> clientOptional = clientService.getClientByDn(request.getDn());

        if (clientOptional.isEmpty()) {
            log.warn("Unable to find client: {}", request.getDn());
            return null;
        }

        Client client = clientOptional.get();

        if (!StringUtils.hasText(client.getPublicKey())) {
            client.setPublicKey(request.getPublicKey());
        }

        clientService.checkPasswordAndClientSecret(client);
        return client;
    }
}
