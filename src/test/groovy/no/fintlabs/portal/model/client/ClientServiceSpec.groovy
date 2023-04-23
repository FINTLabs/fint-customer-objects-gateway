package no.fintlabs.portal.model.client

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.oauth.OAuthClient
import no.fintlabs.portal.utilities.SecretService
import org.apache.commons.collections4.CollectionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static no.fintlabs.portal.testutils.ObjectFactory.newClient

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@Transactional
class ClientServiceSpec extends Specification {

    private ClientService clientService

    @Autowired
    private LdapService ldapService

    @Autowired
    private AssetService assetService

    @Autowired
    private ClientDBRepository clientDBRepository

    private Organisation fintlabsOrganisation

    def oauthService


    void setup() {
        oauthService = Mock(NamOAuthClientService)
        clientService = new ClientService(
                new ClientFactory("ou=organisations,o=fint-test"),
                ldapService,
                assetService,
                oauthService,
                new SecretService(),
                clientDBRepository
        )

        fintlabsOrganisation = new Organisation(
                dn: LdapNameBuilder.newInstance("ou=fintlabs_no,ou=organisations,o=fint-test").build(),
                name: "fintlabs_no", primaryAssetId: "fintlabs.no")

        oauthService.addOAuthClient(_ as String) >> new OAuthClient(clientId: "id")
        oauthService.getOAuthClient(_ as String) >> new OAuthClient(clientId: "id", clientSecret: "secret")

        clientService.addClient(newClient(), fintlabsOrganisation)
        clientService.addClient(newClient(), fintlabsOrganisation)
        clientService.addClient(newClient(), fintlabsOrganisation)
        clientService.addClient(newClient(), fintlabsOrganisation)
    }

    void cleanup() {
        clientService.getClients(fintlabsOrganisation.getName()).forEach { clientService.deleteClient(it) }
    }

    def "The ldap and db version of a client should be equal"() {
        given:
        def client = clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get()

        when:
        def dbClient = clientDBRepository.findById(LdapNameBuilder.newInstance(client.getDn()).build())
        def ldapClient = ldapService.getEntry(client.getDn(), Client.class)

        then:
        dbClient.isPresent()
        ldapClient != null

        ldapClient.getDn() == dbClient.get().getDn()
        ldapClient.getName() == dbClient.get().getName()
        ldapClient.getAsset() == dbClient.get().getAsset()
        ldapClient.getAssetId() == dbClient.get().getAssetId()
        ldapClient.getClientId() == dbClient.get().getClientId()
        ldapClient.getNote() == dbClient.get().getNote()
        ldapClient.getShortDescription() == dbClient.get().getShortDescription()
        ldapClient.isManaged() == dbClient.get().isManaged()
        CollectionUtils.isEqualCollection(ldapClient.getComponents(), dbClient.get().getComponents())
        CollectionUtils.isEqualCollection(ldapClient.getAccessPackages(), dbClient.get().getAccessPackages())
    }

    def "A db client should have password, client secret and public key"() {
        given:
        def client = clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get()

        when:
        def dbClient = clientDBRepository.findById(LdapNameBuilder.newInstance(client.getDn()).build())

        then:
        dbClient.isPresent()
        dbClient.get().getPassword()
        dbClient.get().getClientSecret()
        dbClient.get().getPublicKey()
    }

    def "Get clients should return all clients for the organisation"() {
        when:
        def clients = clientService.getClients(fintlabsOrganisation.getName())

        then:
        clients.size() == 4
    }

    def "Get client by name should return the client with the specified name"() {
        given:
        def client = clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get()


        when:
        def clientByName = clientService.getClientByName(client.getName(), fintlabsOrganisation)

        then:
        clientByName.isPresent()
        clientByName.get().getName() == client.getName()
    }

    def "Get client secret should return the client secret"() {
        when:
        def secret = clientService.getClientSecret(clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get())

        then:
        secret
    }

    def "When update client both ldap and db version should still be equal"() {
        given:
        def client = clientService.getClientByDn(clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()
        client.setNote("test")

        when:
        clientService.updateClient(client)
        def ldap = ldapService.getEntry(client.getDn(), Client.class)
        def db = clientDBRepository.findById(LdapNameBuilder.newInstance(client.getDn()).build()).get()

        then:
        ldap.getNote() == db.getNote()
    }

    def "When updating client the password should not change"() {
        given:
        def client = clientService.getClientByDn(clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()

        when:
        def updateClient = clientService.updateClient(client).get()

        then:
        client.getPassword() == updateClient.getPassword()
    }

    def "When reset password the password should change"() {
        given:
        def client = clientService.getClientByDn(clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()
        def oldPassword = client.getPassword()

        when:
        clientService.resetAndEncryptPassword(client, client.getPublicKey())
        def newPassword = clientService.getClientByDn(client.getDn()).get().getPassword()

        then:
        oldPassword != newPassword
    }

    def "When deleting a client it should be removed from both ldap and db"() {
        given:
        def client = clientService.getClientByDn(clientService.getClients(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()

        when:
        clientService.deleteClient(client)
        def ldap = ldapService.getEntry(client.getDn(), Client.class)
        def db = clientDBRepository.findById(LdapNameBuilder.newInstance(client.getDn()).build())

        then:
        ldap == null
        db.isEmpty()

    }
}
