package no.fintlabs.portal.utilities

import no.fintlabs.portal.testutils.DecryptionHelper
import spock.lang.Specification

import java.security.KeyPair
import java.security.KeyPairGenerator

class SecretServiceSpec extends Specification {

    def secretService
    def publicKey
    def privateKey

    void setup() {
        secretService = new SecretService()

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        KeyPair pair = generator.generateKeyPair()

        publicKey = pair.getPublic()
        privateKey = pair.getPrivate()
    }

    def "Decrypted password should be equal to clear text password"() {
        given:
        def clearTextPassword = "topsecret"

        when:

        def encryptPassword = secretService.encrypt(clearTextPassword, Base64.getEncoder().encodeToString(publicKey.getEncoded()))
        def decryptedPassword = DecryptionHelper.decrypt(privateKey, encryptPassword)

        then:
        encryptPassword
        decryptedPassword == clearTextPassword
    }

    def "Generate secret should return a random string of 32 characters"() {
        when:
        def secret = secretService.generateSecret()

        then:
        secret.length() == 32

    }
}
