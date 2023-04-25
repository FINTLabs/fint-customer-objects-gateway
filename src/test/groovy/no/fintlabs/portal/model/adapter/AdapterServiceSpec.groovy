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
        ldapAdapter.getAsset() == dbAdapter.get().getAsset()
        ldapAdapter.getAssetId() == dbAdapter.get().getAssetId()
        ldapAdapter.getClientId() == dbAdapter.get().getClientId()
        ldapAdapter.getNote() == dbAdapter.get().getNote()
        ldapAdapter.getShortDescription() == dbAdapter.get().getShortDescription()
        ldapAdapter.isManaged() == dbAdapter.get().isManaged()
        CollectionUtils.isEqualCollection(ldapAdapter.getComponents(), dbAdapter.get().getComponents())
        CollectionUtils.isEqualCollection(ldapAdapter.getAccessPackages(), dbAdapter.get().getAccessPackages())
    }

//    private adapterService
//    private ldapService
//    private adapterObjectService
//    private oauthService
//    private assetService
//
//    def setup() {
//        def organisationBase = "ou=org,o=fint"
//        ldapService = Mock(LdapService)
//        assetService = Mock(AssetService)
//        adapterObjectService = new AdapterFactory(new SecretService(), organisationBase)
//        oauthService = Mock(NamOAuthClientService)
//        adapterService = new AdapterService(
//                adapterObjectService,
//                ldapService,
//                oauthService,
//                assetService,
//                new SecretService()
//        )
//
//    }
//
//    def "Add Adapter"() {
//        given:
//        def adapter = ObjectFactory.newAdapter()
//
//        when:
//        def created = adapterService.addAdapter(adapter, new Organisation(name: "name"))
//
//        then:
//        created.isPresent()
//        adapter.dn != null
//        adapter.name != null
//        1 * ldapService.createEntry(_ as Adapter) >> true
//        1 * ldapService.getEntry(_ as String, _ as Class) >> adapter
//        1 * oauthService.addOAuthClient(_ as String) >> new OAuthClient()
//    }
//
//    def "Get Adapters"() {
//        when:
//        def adapters = adapterService.getAdapters(UUID.randomUUID().toString())
//
//        then:
//        adapters.size() == 2
//        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newAdapter(), ObjectFactory.newAdapter())
//        //2 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
//    }
//
//    def "Get Adapter"() {
//        when:
//        def adapter = adapterService.getAdapter(UUID.randomUUID().toString(), UUID.randomUUID().toString())
//
//        then:
//        adapter.isPresent()
//        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newAdapter()
//        //1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
//    }
//
//    def "Update Adapter"() {
//        given:
//        def adapter = ObjectFactory.newAdapter()
//        adapter.setDn("cn=name")
//
//        when:
//        def updated = adapterService.updateAdapter(adapter)
//
//        then:
//        updated.isPresent()
//        1 * ldapService.updateEntry(_ as Adapter) >> true
//        1 * ldapService.getEntry(_ as String, _ as Class) >> adapter
//
//    }
//
//    def "Get Adapter OpenID Secret"() {
//        when:
//        def adapter = adapterService.getAdapter(UUID.randomUUID().toString(), UUID.randomUUID().toString())
//        def secret = adapterService.getClientSecret(adapter.get())
//
//        then:
//        secret
//        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newAdapter()
//        1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
//    }
//
//    def "Delete Adapter"() {
//        when:
//        adapterService.deleteAdapter(ObjectFactory.newAdapter())
//
//        then:
//        1 * ldapService.deleteEntry(_ as Adapter)
//    }
//
//    def "Reset Adapter Password"() {
//        given:
//        def adapter = ObjectFactory.newAdapter()
//
//        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA")
//        generator.initialize(2048)
//        KeyPair pair = generator.generateKeyPair()
//
//        when:
//        adapterService.resetAdapterPassword(adapter, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()))
//
//        then:
//        1 * ldapService.updateEntry(_ as Adapter)
//    }

}
