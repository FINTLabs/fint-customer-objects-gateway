package no.fintlabs.portal.model;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.portal.ldap.BasicLdapEntry;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class FintCustomerObjectRequestBroker<E extends FintCustomerObjectEvent<T>, T extends BasicLdapEntry> {

    private final OrganisationService organisationService;

    private Map<String, FintCustomerObjectHandler<T, E>> actionsHandlerMap;
    private final Collection<FintCustomerObjectHandler<T, E>> handlers;

    public FintCustomerObjectRequestBroker(OrganisationService organisationService,
                                           Collection<FintCustomerObjectHandler<T, E>> handlers,
                                           RequestConsumerFactoryService requestConsumerFactoryService,
                                           RequestTopicService requestTopicService) {
        this.organisationService = organisationService;
        this.handlers = handlers;
        createCustomerObjectRequestConsumer(requestConsumerFactoryService, requestTopicService);

    }

    @SuppressWarnings("unchecked")
    private Class<E> getEventTypeClass() {
        return (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    private Class<T> getCustomerObjectTypeClass() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
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
        log.info("Registered {} handlers ({})", handlers.size(), handlers.stream().map(FintCustomerObjectHandler::operation).collect(Collectors.joining(", ")));

    }

    private ReplyProducerRecord<E> processEvent(ConsumerRecord<String, E> consumerRecord) {

        log.info("{} event received for : {}", consumerRecord.value().getOperationWithType(), consumerRecord.value().getOrganisationObjectName());
        E event = consumerRecord.value();
        try {
            T customerObject = actionsHandlerMap
                    .get(consumerRecord.value().getOperationWithType())
                    .apply(consumerRecord,
                            organisationService
                                    .getOrganisation(consumerRecord.value().getOrganisationObjectName())
                                    .orElseThrow(() -> new RuntimeException("Unable to find organisation " + consumerRecord.value().getOrgId()))
                    );
            event.setObject(customerObject);

            return ReplyProducerRecord
                    .<E>builder()
                    .value(event)
                    .build();

        } catch (Exception e) {
            log.error("An error occurred when handling event {}:", consumerRecord.value().getOperationWithType());
            log.error(e.getMessage());

            event.setErrorMessage(e.getMessage());
            event.setObject(null);

            return ReplyProducerRecord
                    .<E>builder()
                    .value(event)
                    .build();
        }


    }

    private void createCustomerObjectRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService) {

        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .domainContext("fint-customer-objects")
                .resource(getCustomerObjectTypeClass().getSimpleName().toLowerCase())
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        requestConsumerFactoryService.createFactory(
                getEventTypeClass(),
                getEventTypeClass(),
                this::processEvent,
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }


}
