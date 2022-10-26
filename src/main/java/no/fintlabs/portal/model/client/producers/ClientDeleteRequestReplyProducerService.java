package no.fintlabs.portal.model.client.producers;

import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.portal.model.client.ClientRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientDeleteRequestReplyProducerService extends ClientRequestReplyProducer<ClientRequest, ClientRequest> {

    public ClientDeleteRequestReplyProducerService(
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService,
            @Value("${fint.application-id}") String applicationId
    ) {
        super(
                requestProducerFactory,
                replyTopicService,
                applicationId,
                "client-delete",
                "client",
                ClientRequest.class,
                ClientRequest.class
        );
    }

}
