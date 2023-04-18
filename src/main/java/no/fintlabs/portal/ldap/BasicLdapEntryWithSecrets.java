package no.fintlabs.portal.ldap;

public interface BasicLdapEntryWithSecrets extends BasicLdapEntry {

    String getPublicKey();

    void setPublicKey(String publicKey);

}
