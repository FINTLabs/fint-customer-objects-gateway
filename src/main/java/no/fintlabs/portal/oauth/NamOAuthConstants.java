package no.fintlabs.portal.oauth;

import com.google.common.collect.Lists;

import java.util.List;

public class NamOAuthConstants {

    public static final String SCOPE = "urn:netiq.com:nam:scope:oauth:registration:full";
    public static final List<String> GRANT_TYPE = Lists.newArrayList("password" /*, "refresh_token"*/);
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String ACCESS_TOKEN_URL_TEMPLATE = "https://%s//nidp/oauth/nam/token";
    public static final String CLIENT_REGISTRATION_URL_TEMPLATE = "http://%s/nidp/oauth/nam/clients/";
    public static final String CLIENT_URL_TEMPLATE = "http://%s/nidp/oauth/nam/clients/{clientId}";
    public static final String APPLICATION_TYPE = "web";

    public static final String DEFAULT_REDIRECT_URI = "https://dummy.com";
    public static final String RESPONSE_TYPE = "token";
    public static final List<String> CORS_DOMAINS = Lists.newArrayList("beta.felleskomponent.no", "api.felleskomponent.no");
}
