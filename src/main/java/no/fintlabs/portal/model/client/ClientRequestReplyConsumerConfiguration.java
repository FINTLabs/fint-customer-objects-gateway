package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.portal.exceptions.EntityNotFoundException;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.component.ComponentService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.function.Function;

@Slf4j
@Configuration
public class ClientRequestReplyConsumerConfiguration {

    private final OrganisationService organisationService;
    private final ClientService clientService;
    private final RequestConsumerFactoryService requestConsumerFactoryService;
    private final RequestTopicService requestTopicService;
    private final ComponentService componentService;

    public ClientRequestReplyConsumerConfiguration(
            OrganisationService organisationService,
            ClientService clientService,
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            ComponentService componentService,
            @Value("${fint.application-id}") String applicationId
    ) {
        this.organisationService = organisationService;
        this.clientService = clientService;
        this.requestConsumerFactoryService = requestConsumerFactoryService;
        this.requestTopicService = requestTopicService;
        this.componentService = componentService;
    }

    private <V, R> ConcurrentMessageListenerContainer<String, V> initConsumer(
            String resourceReference,
            String parameterNameReferance,
            Class<V> valueClass,
            Class<R> replyValueClass,
            Function<ConsumerRecord<String, V>, ReplyProducerRecord<R>> consumerRecord
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource(resourceReference)
                .parameterName(parameterNameReferance)
                .build();

        requestTopicService.ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                valueClass,
                replyValueClass,
                consumerRecord,
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientRequest> createOrUpdateClient() {
        return initConsumer(
                "client-create",
                "client",
                ClientRequest.class,
                Client.class,
                consumerRecord -> {
                    ClientRequest clientRequest = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientRequest.getOrgName());

                    Client client = clientService
                            .getClientBySimpleName(clientRequest.getName(), organisation)
                            .orElseGet(() -> createNewClient(clientRequest));

                    if (isNewClient(client)) {
                        if (clientService.addClient(client, organisation)) {
                            client = clientService.getClientBySimpleName(clientRequest.getName(), organisation).orElseThrow();
                            log.info("Client " + client.getClientId() + " added successfully");
                        } else {
                            log.error("Client " + client.getClientId() + " was not added");
                        }
                    }

                    if (clientRequest.getNote() != null) {
                        client.setNote(clientRequest.getNote());
                    }

                    if (clientRequest.getShortDescription() != null) {
                        client.setShortDescription(clientRequest.getShortDescription());
                    }

                    updateComponents(clientRequest, client);

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }

    private void updateComponents(ClientRequest clientRequest, Client client) {
        client.getComponents().forEach(c -> {
            Component component = componentService.getComponentByDn(c).orElseThrow();
            componentService.unLinkClient(component, client);
        });

        clientRequest.getComponents().forEach(c -> {
            Component component = componentService.getComponentByName(c).orElseThrow();
            componentService.linkClient(component, client);
        });
    }

    private boolean isNewClient(Client client) {
        return StringUtils.isEmpty(client.getClientId());
    }

    private Client createNewClient(ClientRequest clientRequest) {
        Client c = new Client();
        c.setName(clientRequest.getName());
        c.setNote(clientRequest.getNote());
        c.setShortDescription(clientRequest.getShortDescription());
        return c;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientRequest> deleteClient() {
        return initConsumer(
                "client-delete",
                "client",
                ClientRequest.class,
                Client.class,
                consumerRecord -> {
                    ClientRequest clientRequest = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientRequest.getOrgName());
                    Client client = clientService.getClient(clientRequest.getName(), organisation.getName()).orElseThrow(() -> new EntityNotFoundException("Client " + clientRequest.getName() + " not found"));

                    clientService.deleteClient(client);

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientRequest> getClient() {
        return initConsumer(
                "client-get",
                "client",
                ClientRequest.class,
                Client.class,
                consumerRecord -> {
                    ClientRequest clientRequest = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientRequest.getOrgName());

                    Client client = clientService.getClient(clientRequest.getName(), organisation.getName()).orElseThrow(() -> new EntityNotFoundException("Client " + clientRequest.getName() + " not found"));

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }


}
