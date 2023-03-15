package no.fintlabs.portal.model.client;

//@Component
public class DeleteClientHandler {
    //private final ClientService clientService;

//    public DeleteClientHandler(EventTopicService eventTopicService, EntityTopicService entityTopicService, EventConsumerFactoryService consumer, OrganisationService organisationService, EntityProducerFactory entityProducerFactory,  ClientService clientService) {
//        super(eventTopicService, entityTopicService, consumer, organisationService, entityProducerFactory, handlers, Client.class);
//        this.clientService = clientService;
//    }
//
//
//    @Override
//    public Function<Organisation, ClientEvent> handleObject(ConsumerRecord<String, ClientEvent> consumerRecord, ClientEvent response) {
//        return organisation -> clientService.deleteClient(consumerRecord.value().getObject()).map(client -> {
//            response.setStatus(FintCustomerObjectEvent.Status
//                    .builder()
//                    .successful(true)
//                    .build());
//            return response;
//        }).orElseThrow(() -> new RuntimeException("An unexpected error occurred while deleting client"));
//    }
}
