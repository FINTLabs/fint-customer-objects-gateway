package no.fintlabs.portal.utilities

import spock.lang.Specification


class PasswordUtilitySpec extends Specification {
    def "Generate new password"() {
        when:
        def password = PasswordUtility.newPassword()

        then:
        password != null
        password.length() == 32

    }
}
