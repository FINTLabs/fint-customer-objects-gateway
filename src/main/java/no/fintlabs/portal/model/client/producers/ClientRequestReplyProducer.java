package no.fintlabs.portal.model.client.producers;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Optional;

public abstract class ClientRequestReplyProducer<V, R> {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<V, R> requestProducer;

    public ClientRequestReplyProducer(
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService,
            String applicationId,
            String topicName,
            String parameterName,
            Class<V> dataIn,
            Class<R> dataOut
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .applicationId(applicationId)
                .resource(topicName)
                .build();
        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource(topicName)
                .parameterName(parameterName)
                .build();

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                dataIn,
                dataOut,
                RequestProducerConfiguration.
                        builder()
                        .defaultReplyTimeout(Duration.ofSeconds(15))
                        .build()
        );
    }

    public Optional<R> get(V value) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord
                        .<V>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(value)
                        .build()
        ).map(ConsumerRecord::value);
    }

}
