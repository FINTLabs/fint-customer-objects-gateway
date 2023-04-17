# FLAIS Customer Object Gateway

Supports CRUD of:

* Client
* Adapter

## Local development

> You need to have a secure connection to the LDAP server before you start the application.

When running in `test-mode` the gateway will start two controllers where you can get test json objects to submit on the
event topics. You can import insomnia.json into your Insomnia.


### Submit an client event

* Do a `POST` on http://localhost/client (in Insomnia) to get a sample `Create Client` event.
* Take the response and add a single message to the `flais-io.customer-object-gateway.event.client` topic.
* When the client is created you will find it on the topic `flais-io.customer-object-gateway.entity.client`.
* You can use the object form the entity topic and `POST` to the `Client decrypt` endpoint in Insomnia to decrypt the
  client secret and password.

> Note that you can change the input type for a topic in Offset Explorer by going to the topic -> Properties and change
> the Content type.

### Submit an adapter event
The procedure is the same as for client.

## Properties

| Property                          | Description                                      |
|-----------------------------------|--------------------------------------------------|
| fint.customer-object-gateway.mode | If set to `test` a test controller is started    |
| fint.ldap.access.template-base    | LDAP DN for Access templates base                |
| fint.ldap.component-base          | LDAP DN for Component base                       |
| fint.ldap.contact-base            | LDAP DN for Contact base                         |
| fint.ldap.organisation-base       | LDAP DN for Organisation base                    |
| fint.ldap.password                | Password for LDAP user                           |
| fint.ldap.url                     | URL for LDAP server. E.g. ldap://localhost:389   |
| fint.ldap.user                    | LDAP users with necessary rights                 |
| fint.nam.oauth.clientId           | Client id to use for OAuth client creation       |
| fint.nam.oauth.clientSecret       | Client secret to use for OAuth client creation   |
| fint.nam.oauth.idp-hostname       | IDP hostname. E.g. `idp.test.felleskomponent.no` |
| fint.nam.oauth.password           | Password for OAuth client creation user          |
| fint.nam.oauth.username           | User for OAuth client creation user              |



