package no.fintlabs.portal.model.client.producers;

import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientCreateRequestReplyProducerService extends ClientRequestReplyProducer<ClientDto, Client> {

    public ClientCreateRequestReplyProducerService(
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService,
            @Value("${fint.application-id}") String applicationId
    ) {
        super(
                requestProducerFactory,
                replyTopicService,
                applicationId,
                "client-create",
                "client",
                ClientDto.class,
                Client.class
        );
    }

}
