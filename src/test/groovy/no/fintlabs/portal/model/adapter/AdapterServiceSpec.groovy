package no.fintlabs.portal.model.adapter

import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.asset.AssetService
import no.fintlabs.portal.model.organisation.Organisation
import no.fintlabs.portal.oauth.NamOAuthClientService
import no.fintlabs.portal.oauth.OAuthClient
import no.fintlabs.portal.testutils.ObjectFactory
import spock.lang.Specification

class AdapterServiceSpec extends Specification {

    private adapterService
    private ldapService
    private adapterObjectService
    private oauthService
    private assetService

    def setup() {
        def organisationBase = "ou=org,o=fint"
        ldapService = Mock(LdapService)
        assetService = Mock(AssetService)
        adapterObjectService = new AdapterFactory(organisationBase: organisationBase)
        oauthService = Mock(NamOAuthClientService)
        adapterService = new AdapterService(
                adapterFactory: adapterObjectService,
                ldapService: ldapService,
                namOAuthClientService: oauthService,
                assetService: assetService
        )

    }

    def "Add Adapter"() {
        given:
        def adapter = ObjectFactory.newAdapter()

        when:
        def created = adapterService.addAdapter(adapter, new Organisation(name: "name"))

        then:
        created.isPresent()
        adapter.dn != null
        adapter.name != null
        1 * ldapService.createEntry(_ as Adapter) >> true
        1 * ldapService.getEntry(_ as String, _ as Class) >> adapter
        1 * oauthService.addOAuthClient(_ as String) >> new OAuthClient()
    }

    def "Get Adapters"() {
        when:
        def adapters = adapterService.getAdapters(UUID.randomUUID().toString())

        then:
        adapters.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newAdapter(), ObjectFactory.newAdapter())
        //2 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Get Adapter"() {
        when:
        def adapter = adapterService.getAdapter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        then:
        adapter.isPresent()
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newAdapter()
        //1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Update Adapter"() {
        given:
        def adapter = ObjectFactory.newAdapter()
        adapter.setDn("cn=name")

        when:
        def updated = adapterService.updateAdapter(adapter)

        then:
        updated.isPresent()
        1 * ldapService.updateEntry(_ as Adapter) >> true
        1 * ldapService.getEntry(_ as String, _ as Class) >> adapter

    }

    def "Get Adapter OpenID Secret"() {
        when:
        def adapter = adapterService.getAdapter(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        def secret = adapterService.getAdapterSecret(adapter.get())

        then:
        secret
        1 * ldapService.getEntry(_ as String, _ as Class) >> ObjectFactory.newAdapter()
        1 * oauthService.getOAuthClient(_ as String) >> ObjectFactory.newOAuthClient()
    }

    def "Delete Adapter"() {
        when:
        adapterService.deleteAdapter(ObjectFactory.newAdapter())

        then:
        1 * ldapService.deleteEntry(_ as Adapter)
    }

    def "Reset Adapter Password"() {
        given:
        def adapter = ObjectFactory.newAdapter()

        when:
        adapterService.resetAdapterPassword(adapter, "FIXME")

        then:
        1 * ldapService.updateEntry(_ as Adapter)
    }

}
