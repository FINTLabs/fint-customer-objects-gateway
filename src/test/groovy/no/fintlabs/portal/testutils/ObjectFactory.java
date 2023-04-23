package no.fintlabs.portal.testutils;

import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.asset.Asset;
import no.fintlabs.portal.model.client.Client;
import no.fintlabs.portal.model.component.Component;
import no.fintlabs.portal.model.contact.Contact;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.oauth.OAuthClient;

import java.util.UUID;

public enum ObjectFactory {
    ;

    public static Component newComponent() {
        Component component = new Component();
        //component.setDn("ou=comp1,ou=comp,o=fint");
        component.setName("compTest");
        component.setDescription("Created by test");
        return component;
    }

    public static Adapter newAdapter() {
        Adapter adapter = new Adapter();
        adapter.setName("TestAdapter");
        adapter.setNote("Test adapter for test organisation");
        adapter.setShortDescription("Test Adapter");
        adapter.setClientId("123");
        return adapter;
    }

    public static Client newClientWithDn() {
        Client client = new Client();
        client.setDn("cn=name");
        client.setName("TestClient");
        client.setNote("Test client for test organisation");
        client.setShortDescription("Test Client");
        client.setClientId("123");
        client.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx+1mDlIElhba0nN0wY7KWkctVsje+TYaycLZDoqW8sBBLLfU7icmRZtCtUnS1HptBKqUVYY94WKPfWEwCqP9EcWZr0wQ7iX4dBN/RYNsLwOknT520UkjliuY3ZIelsE0o8k/bcW2oqzHDrvtIVGfmUz8pS5+laIYN3EwyFKI/oJkIv2VPtD0XYorquckas/mMzk387kfNJIOn5o1Riuyd49NJeI0XJpgqBmPfHUsCcmmzucvXcK4q32RasE9o7l6MiFEhcK/evgBBONCFTobaVuo4Gu90+Y+laStKRRSMQNDqIYS+zo8id2TOSkEbG0pmZuEM5aSovLxkKjGNEN65wIDAQAB");
        return client;
    }

    public static Client newClient() {
        Client client = new Client();
        client.setName(UUID.randomUUID().toString());
        client.setNote("Test client for test organisation");
        client.setShortDescription("Test Client");
        //client.setClientId("123");
        client.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx+1mDlIElhba0nN0wY7KWkctVsje+TYaycLZDoqW8sBBLLfU7icmRZtCtUnS1HptBKqUVYY94WKPfWEwCqP9EcWZr0wQ7iX4dBN/RYNsLwOknT520UkjliuY3ZIelsE0o8k/bcW2oqzHDrvtIVGfmUz8pS5+laIYN3EwyFKI/oJkIv2VPtD0XYorquckas/mMzk387kfNJIOn5o1Riuyd49NJeI0XJpgqBmPfHUsCcmmzucvXcK4q32RasE9o7l6MiFEhcK/evgBBONCFTobaVuo4Gu90+Y+laStKRRSMQNDqIYS+zo8id2TOSkEbG0pmZuEM5aSovLxkKjGNEN65wIDAQAB");
        return client;
    }

    public static Organisation newOrganisation() {
        Organisation organisation = new Organisation();
        organisation.setName("TestOrganisation");
        organisation.setOrgNumber("1111111111");
        organisation.setDisplayName("Test organisation");
        organisation.setDn("ou=testOrg,ou=org,o=fint");
        return organisation;
    }

    public static Contact newContact(String nin) {
        Contact contact = new Contact();
        contact.setNin(nin);
        contact.setDn("cn="+nin+",ou=contacts,o=fint");
        contact.addRole("ROLE_ADAPTER");
        return contact;
    }

    public static Asset newAsset() {
        Asset asset = new Asset();
        asset.setName("assetName");
        asset.setAssetId("test.no");
        asset.setPrimaryAsset(true);
        return asset;
    }

    public static OAuthClient newOAuthClient() {
        OAuthClient oAuthClient = new OAuthClient();
        oAuthClient.setClientId("123");
        oAuthClient.setClientSecret("secret");

        return oAuthClient;
    }
}
