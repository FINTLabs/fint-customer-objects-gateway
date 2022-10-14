package no.fintlabs.portal.utilities;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LdapTimestamp {
    public static final String LDAP_TIMESTAMP_FORMAT = "yyyyMMddHHmmss'Z'";

    public static LocalDateTime toLocalTimeDate(String ldapTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(LDAP_TIMESTAMP_FORMAT);

        return LocalDateTime.parse(ldapTime, dateTimeFormatter);
    }
}