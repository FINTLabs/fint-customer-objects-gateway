package no.fintlabs.portal.utilities

import spock.lang.Specification

class LdapTimestampSpec extends Specification {

    def "LDAP timestamp should return LocaleTimeDate"() {

        when:
        def time = LdapTimestamp.toLocalTimeDate("20201105131215Z")

        then:
        time.getYear() == 2020
        time.getMonthValue() == 11
        time.getDayOfMonth() == 5
        time.getHour() == 13
        time.getMinute() == 12
        time.getSecond() == 15

    }
}
