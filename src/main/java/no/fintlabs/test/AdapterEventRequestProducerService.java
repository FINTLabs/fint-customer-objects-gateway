package no.fintlabs.test;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.portal.model.adapter.AdapterEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class AdapterEventRequestProducerService {
    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<AdapterEvent, AdapterEvent> requestProducer;

    public AdapterEventRequestProducerService(
            @Value("${fint.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .domainContext("fint-customer-objects")
                .resource("adapter")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .domainContext("fint-customer-objects")
                .resource("adapter")
                .build();

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                AdapterEvent.class,
                AdapterEvent.class,
                RequestProducerConfiguration
                        .builder()
                        .defaultReplyTimeout(Duration.ofMinutes(2))
                        .build()
        );
    }

    public Optional<AdapterEvent> get(AdapterEvent adapterEvent) {
        return requestProducer.requestAndReceive(
                        RequestProducerRecord.<AdapterEvent>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .value(adapterEvent)
                                .build()
                )
                .map(ConsumerRecord::value);
    }
}
