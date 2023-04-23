package no.fintlabs.portal.model.asset

import no.fintlabs.portal.exceptions.CreateEntityMismatchException
import no.fintlabs.portal.ldap.LdapService
import no.fintlabs.portal.model.adapter.Adapter
import no.fintlabs.portal.model.client.Client
import no.fintlabs.portal.testutils.ObjectFactory
import spock.lang.Specification

class AssetServiceSpec extends Specification {

    private assetService
    private ldapService

    def setup() {
        ldapService = Mock(LdapService)
        assetService = new AssetService(ldapService: ldapService)
    }

    def "Add Sub Asset"() {
        given:
        def asset = ObjectFactory.newAsset()
        def organisation = ObjectFactory.newOrganisation()
        asset.assetId = "test.no"
        ldapService.getAll(_,_) >> List.of()

        when:
        assetService.addSubAsset(asset, organisation)

        then:
        asset.dn
        asset.name
        asset.organisation == organisation.dn
        1 * ldapService.createEntry(_ as Asset) >> true
    }

    def "Add Sub Asset with invalid char"() {
        given:
        def asset = ObjectFactory.newAsset()
        def organisation = ObjectFactory.newOrganisation()
        asset.assetId = "te%st.no"
        ldapService.getAll(_,_) >> List.of()

        when:
        assetService.addSubAsset(asset, organisation)

        then:
        thrown IllegalArgumentException
    }

    def "Add Sub Asset fails"() {
        given:
        def asset = ObjectFactory.newAsset()
        def organisation = ObjectFactory.newOrganisation()
        asset.assetId = "test.no"
        ldapService.getAll(_,_) >> List.of()
        ldapService.createEntry(_) >> false

        when:
        assetService.addSubAsset(asset, organisation)

        then:
        thrown CreateEntityMismatchException
    }

    def "Update Asset"() {
        given:
        def asset = ObjectFactory.newAsset()
        asset.assetId = "test.no"

        when:
        def updated = assetService.updateAsset(asset)

        then:
        updated
        1 * ldapService.updateEntry(_ as Asset) >> true
    }

    def "Remove Asset"() {
        given:
        def asset = ObjectFactory.newAsset()

        when:
        assetService.removeAsset(asset)

        then:
        1 * ldapService.deleteEntry(_ as Asset)
    }

    def "Get Assets"() {
        given:
        def organisation = ObjectFactory.newOrganisation()

        when:
        def assets = assetService.getAssets(organisation)

        then:
        assets.size() == 2
        1 * ldapService.getAll(_ as String, _ as Class) >> Arrays.asList(ObjectFactory.newAsset(), ObjectFactory.newAsset())
    }

    def "Link Client to Asset"() {
        given:
        def asset = ObjectFactory.newAsset()
        asset.assetId = "test.no"
        def client = ObjectFactory.newClientWithDn()
        client.dn = "cn=xyzzy,ou=clients,ou=test,ou=org,o=fint"

        when:
        assetService.linkClientToAsset(asset,client)

        then:
        asset.clients.any { it.contains('xyzzy')}
        client.assetId == 'test.no'
        1 * ldapService.updateEntry(_ as Asset) >> true
        1 * ldapService.updateEntry(_ as Client) >> true
    }

    def "Unlink Client from Asset"() {
        given:
        def asset = ObjectFactory.newAsset()
        asset.assetId = "test.no"
        def client1 = ObjectFactory.newClientWithDn()
        client1.dn = "cn=xyzzy,ou=clients,ou=test,ou=org,o=fint"
        assetService.linkClientToAsset(asset,client1)
        def client2 = ObjectFactory.newClientWithDn()
        client2.dn = "cn=abcabc,ou=clients,ou=test,ou=org,o=fint"
        assetService.linkClientToAsset(asset,client2)

        when:
        assetService.unlinkClientFromAsset(asset,client1)

        then:
        asset.clients.size() == 1
        asset.clients.any { it.contains('abcabc')}
        client2.assetId == 'test.no'
        client1.assetId == null
        1 * ldapService.updateEntry(_ as Asset) >> true
        1 * ldapService.updateEntry(_ as Client) >> true

    }


    def "Link Adapter to Asset"() {
        given:
        def asset = ObjectFactory.newAsset()
        asset.dn = "cn=test_no,ou=assets,ou=test,ou=org,o=fint"
        def adapter = ObjectFactory.newAdapter()
        adapter.dn = "cn=xyzzy,ou=adapters,ou=test,ou=org,o=fint"

        when:
        assetService.linkAdapterToAsset(asset,adapter)

        then:
        asset.adapters.any { it.contains('xyzzy')}
        adapter.assets.any { it =~ /test_no/ }
        1 * ldapService.updateEntry(_ as Asset) >> true
        1 * ldapService.updateEntry(_ as Adapter) >> true
    }

    def "Unlink Adapter from Asset"() {
        def asset = ObjectFactory.newAsset()
        asset.dn = "cn=test_no,ou=assets,ou=test,ou=org,o=fint"
        def adapter1 = ObjectFactory.newAdapter()
        adapter1.dn = "cn=xyzzy,ou=adapters,ou=test,ou=org,o=fint"
        assetService.linkAdapterToAsset(asset,adapter1)
        def adapter2 = ObjectFactory.newAdapter()
        adapter2.dn = "cn=abcabc,ou=adapters,ou=test,ou=org,o=fint"
        assetService.linkAdapterToAsset(asset,adapter2)

        when:
        assetService.unlinkAdapterFromAsset(asset,adapter1)

        then:
        asset.adapters.size() == 1
        asset.adapters.any { it.contains('abcabc')}
        adapter1.assets.isEmpty()
        adapter2.assets.any { it =~ /test_no/ }
        1 * ldapService.updateEntry(_ as Asset) >> true
        1 * ldapService.updateEntry(_ as Adapter) >> true
    }

}
