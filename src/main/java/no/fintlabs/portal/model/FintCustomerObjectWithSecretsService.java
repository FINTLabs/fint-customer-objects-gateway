package no.fintlabs.portal.model;

import no.fintlabs.portal.ldap.BasicLdapEntryWithSecrets;

public interface FintCustomerObjectWithSecretsService<T extends BasicLdapEntryWithSecrets> {

    void encryptClientSecret(T object, String publicKeyString);

    void encryptPassword(T object, String privateKeyString);


}
