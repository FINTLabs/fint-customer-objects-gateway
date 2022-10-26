package no.fintlabs.portal.model.client.producers;

import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.client.ClientRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientCreateRequestReplyProducerService extends ClientRequestReplyProducer<ClientRequest, Client> {

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
                ClientRequest.class,
                Client.class
        );
    }

}
