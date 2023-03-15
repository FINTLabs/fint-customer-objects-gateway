package no.fintlabs.portal.model.client;

//@Component
public class GetClientHandler {
//    private final ClientService clientService;
//
//    public GetClientHandler(EventTopicService eventTopicService, EntityTopicService entityTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, EntityProducerFactory entityProducerFactory, ClientService clientService) {
//        super(eventTopicService, entityTopicService, consumer, organisationService, entityProducerFactory, handlers, Client.class);
//        this.clientService = clientService;
//    }
//
//
//    @Override
//    public Function<Organisation, ClientEvent> handleObject(ConsumerRecord<String, ClientEvent> consumerRecord, ClientEvent response) {
//        return organisation -> clientService.getClient(consumerRecord.value().getObject().getName(), organisation.getName())
//                .map(client -> {
//                    response.setObject(client);
//                    response.setStatus(FintCustomerObjectEvent.Status.builder().successful(true).build());
//                    return response;
//                })
//                .orElseThrow(() -> new RuntimeException("An unexpected error occurred while getting client."));
//    }
}
