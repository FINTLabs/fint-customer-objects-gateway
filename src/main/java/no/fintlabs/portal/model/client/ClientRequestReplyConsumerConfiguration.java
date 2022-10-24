package no.fintlabs.portal.model.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.portal.exceptions.EntityNotFoundException;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
public class ClientRequestReplyConsumerConfiguration {

    private final OrganisationService organisationService;
    private final ClientService clientService;
    private final RequestConsumerFactoryService requestConsumerFactoryService;
    private final RequestTopicService requestTopicService;

    public ClientRequestReplyConsumerConfiguration(OrganisationService organisationService, ClientService clientService, RequestConsumerFactoryService requestConsumerFactoryService, RequestTopicService requestTopicService) {
        this.organisationService = organisationService;
        this.clientService = clientService;
        this.requestConsumerFactoryService = requestConsumerFactoryService;
        this.requestTopicService = requestTopicService;
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
    public ConcurrentMessageListenerContainer<String, ClientDto> createClient() {
        return initConsumer(
                "client-create",
                "client",
                ClientDto.class,
                Client.class,
                consumerRecord -> {
                    // TODO: 19/10/2022 Needs support for error replies in fint-kafka (both in request producer and consumer)
                    ClientDto clientDto = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientDto.getOrgName());
                    Optional<Client> optionalClient = clientService.getClient(clientDto.getName(), clientDto.getOrgName());

                    Client client = new Client();
                    client.setName(clientDto.getName());
                    client.setNote(clientDto.getNote());
                    client.setShortDescription(clientDto.getShortDescription());

                    if (optionalClient.isEmpty()) {
                        if (clientService.addClient(client, organisation)) {
                            log.info("Client " + client.getClientId() + " added successfully");
                        } else {
                            log.error("Client " + client.getClientId() + " was not added");
                        }
                    }

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientDto> deleteClient() {
        return initConsumer(
                "client-delete",
                "client",
                ClientDto.class,
                Client.class,
                consumerRecord -> {
                    ClientDto clientDto = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientDto.getOrgName());
                    Client client = clientService.getClient(clientDto.getName(), organisation.getName()).orElseThrow(() -> new EntityNotFoundException("Client " + clientDto.getName() + " not found"));

                    clientService.deleteClient(client);

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientDto> getClient() {
        return initConsumer(
                "client-get",
                "client",
                ClientDto.class,
                Client.class,
                consumerRecord -> {
                    ClientDto clientDto = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientDto.getOrgName());

                    Client client = clientService.getClient(clientDto.getName(), organisation.getName()).orElseThrow(() -> new EntityNotFoundException("Client " + clientDto.getName() + " not found"));

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, ClientDto> updateClient() {
        return initConsumer(
                "client-update",
                "client",
                ClientDto.class,
                Client.class,
                consumerRecord -> {
                    ClientDto clientDto = consumerRecord.value();
                    Organisation organisation = organisationService.getOrganisationSync(clientDto.getOrgName());

                    Client client = clientService.getClient(clientDto.getName(), organisation.getName()).orElseThrow(() -> new EntityNotFoundException("Client " + clientDto.getName() + " not found"));

                    System.out.println("test");
                    System.out.println(clientDto);

                    if (clientDto.getNote() != null)
                        client.setNote(clientDto.getNote());
                    if (clientDto.getShortDescription() != null)
                        client.setShortDescription(clientDto.getShortDescription());

                    if (!clientService.updateClient(client)) {
                        throw new EntityNotFoundException(String.format("Could not update client: %s", client.getName()));
                    }

                    return ReplyProducerRecord
                            .<Client>builder()
                            .value(client)
                            .build();

                }
        );
    }


}
