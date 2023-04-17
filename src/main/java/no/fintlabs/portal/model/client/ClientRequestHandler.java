package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.portal.model.FintCustomerObjectRequestHandler;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ClientRequestHandler {

    private final OrganisationService organisationService;
    private final ClientService clientService;

    private Map<String, FintCustomerObjectRequestHandler<Client, ClientEvent>> actionsHandlerMap;
    private final Collection<FintCustomerObjectRequestHandler<Client, ClientEvent>> handlers;

    public ClientRequestHandler(OrganisationService organisationService, ClientService clientService, Collection<FintCustomerObjectRequestHandler<Client, ClientEvent>> handlers) {
        this.organisationService = organisationService;
        this.clientService = clientService;
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {


        actionsHandlerMap = new HashMap<>();
        handlers
                .forEach(fintCustomerObjectEntityHandler ->
                        actionsHandlerMap.put(
                                fintCustomerObjectEntityHandler.operation(),
                                fintCustomerObjectEntityHandler)
                );
        log.info("Registered {} handlers", handlers.size());

    }

    private ReplyProducerRecord<ClientEvent> processEvent(ConsumerRecord<String, ClientEvent> consumerRecord) {

        log.info("{} event received for : {}", consumerRecord.value().getOperationWithType(), consumerRecord.value().getObject());
        ClientEvent clientEvent = consumerRecord.value();
        try {
            Client client = actionsHandlerMap
                    .get(consumerRecord.value().getOperationWithType())
                    .apply(consumerRecord,
                            organisationService
                                    .getOrganisation(consumerRecord.value().getOrganisationObjectName())
                                    .orElseThrow(() -> new RuntimeException("Unable to find organisation " + consumerRecord.value().getOrgId()))
                    );
            clientEvent.setObject(client);

            return ReplyProducerRecord
                    .<ClientEvent>builder()
                    .value(clientEvent)
                    .build();

        } catch (Exception e) {
            log.error("An error occurred when handling event {}:", consumerRecord.value().getObject());
            log.error(e.getMessage());

            clientEvent.setErrorMessage(e.getMessage());
            clientEvent.setObject(null);

            return ReplyProducerRecord
                    .<ClientEvent>builder()
                    .value(clientEvent)
                    .build();
            //throw new RuntimeException("💩🙈");
        }


    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientEvent> clientRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService) {

        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .domainContext("fint-customer-objects")
                .resource("client")
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                ClientEvent.class,
                ClientEvent.class,
                this::processEvent,
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }
}
