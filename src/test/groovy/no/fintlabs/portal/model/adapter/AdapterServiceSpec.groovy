package no.fintlabs.portal.model.adapter

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

import static no.fintlabs.portal.testutils.ObjectFactory.newAdapter

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@Transactional
class AdapterServiceSpec extends Specification {

    private AdapterService adapterService

    @Autowired
    private LdapService ldapService

    @Autowired
    private AssetService assetService

    @Autowired
    private AdapterDBRepository adapterDBRepository

    private Organisation fintlabsOrganisation

    def oauthService

    void setup() {
        oauthService = Mock(NamOAuthClientService)
        adapterService = new AdapterService(
                new AdapterFactory("ou=organisations,o=fint-test"),
                ldapService,
                assetService,
                oauthService,
                new SecretService(),
                adapterDBRepository
        )

        fintlabsOrganisation = new Organisation(
                dn: LdapNameBuilder.newInstance("ou=fintlabs_no,ou=organisations,o=fint-test").build(),
                name: "fintlabs_no", primaryAssetId: "fintlabs.no")

        oauthService.addOAuthClient(_ as String) >> new OAuthClient(clientId: "id")
        oauthService.getOAuthClient(_ as String) >> new OAuthClient(clientId: "id", clientSecret: "secret")

        adapterService.addAdapter(newAdapter(), fintlabsOrganisation)
        adapterService.addAdapter(newAdapter(), fintlabsOrganisation)
        adapterService.addAdapter(newAdapter(), fintlabsOrganisation)
        adapterService.addAdapter(newAdapter(), fintlabsOrganisation)
    }

    void cleanup() {
        adapterService.getAdapters(fintlabsOrganisation.getName()).forEach { adapterService.deleteAdapter(it) }
    }

    def "The ldap and db version of an adapter should be equal"() {
        given:
        def adapter = adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get()

        when:
        def dbAdapter = adapterDBRepository.findById(LdapNameBuilder.newInstance(adapter.getDn()).build())
        def ldapAdapter = ldapService.getEntry(adapter.getDn(), Adapter.class)

        then:
        dbAdapter.isPresent()
        ldapAdapter != null

        ldapAdapter.getDn() == dbAdapter.get().getDn()
        ldapAdapter.getName() == dbAdapter.get().getName()
        ldapAdapter.getAssets() == dbAdapter.get().getAssets()
        ldapAdapter.getAssetIds() == dbAdapter.get().getAssetIds()
        ldapAdapter.getClientId() == dbAdapter.get().getClientId()
        ldapAdapter.getNote() == dbAdapter.get().getNote()
        ldapAdapter.getShortDescription() == dbAdapter.get().getShortDescription()
        ldapAdapter.isManaged() == dbAdapter.get().isManaged()
        CollectionUtils.isEqualCollection(ldapAdapter.getComponents(), dbAdapter.get().getComponents())
        CollectionUtils.isEqualCollection(ldapAdapter.getAccessPackages(), dbAdapter.get().getAccessPackages())
    }

    def "A db should have password, client secret and public key"() {
        given:
        def adapter = adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get()

        when:
        def dbAdapter = adapterDBRepository.findById(LdapNameBuilder.newInstance(adapter.getDn()).build())

        then:
        dbAdapter.isPresent()
        dbAdapter.get().getPassword()
        dbAdapter.get().getClientSecret()
        dbAdapter.get().getPublicKey()
    }

    def "Get adapters should return all adapters for the organisation"() {
        when:
        def adapters = adapterService.getAdapters(fintlabsOrganisation.getName())

        then:
        adapters.size() == 4
    }

    def "Get adapter by name should return the adapter with the specified name"() {
        given:
        def adapter = adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get()

        when:
        def adapterByName = adapterService.getAdapterByName(adapter.getName(), fintlabsOrganisation)

        then:
        adapterByName.isPresent()
        adapterByName.get().getName() == adapter.getName()
    }

    def "Get client secret should return the client secret"() {
        when:
        def secret = adapterService.getClientSecret(adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get())

        then:
        secret
    }

    def "When update adapter both ldap and db version should still be equal"() {
        given:
        def adapter = adapterService.getAdapterByDn(adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()
        adapter.setNote("test")

        when:
        adapterService.updateAdapter(adapter)
        def ldap = ldapService.getEntry(adapter.getDn(), Adapter.class)
        def db = adapterDBRepository.findById(LdapNameBuilder.newInstance(adapter.getDn()).build()).get()

        then:
        ldap.getNote() == db.getNote()
    }

    def "When updating adapter the password should not change"() {
        given:
        def adapter = adapterService.getAdapterByDn(adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()

        when:
        def updateAdapter = adapterService.updateAdapter(adapter).get()

        then:
        adapter.getPassword() == updateAdapter.getPassword()
    }

    def "When reset the password should change"() {
        given:
        def adapter = adapterService.getAdapterByDn(adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()
        def oldPassword = adapter.getPassword()

        when:
        adapterService.resetAndEncryptPassword(adapter, adapter.getPublicKey())
        def newPassword = adapterService.getAdapterByDn(adapter.getDn()).get().getPassword()

        then:
        oldPassword != newPassword
    }

    def "When deleting a client it should be removed from both ldap and db"() {
        given:
        def adapter = adapterService.getAdapterByDn(adapterService.getAdapters(fintlabsOrganisation.getName()).stream().findAny().get().getDn()).get()

        when:
        adapterService.deleteAdapter(adapter)
        def ldap = ldapService.getEntry(adapter.getDn(), Adapter.class)
        def db = adapterDBRepository.findById(LdapNameBuilder.newInstance(adapter.getDn()).build())

        then:
        ldap == null
        db.isEmpty()
    }
}
