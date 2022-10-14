package no.fintlabs.portal.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;

@Configuration
public class LdapConfiguration {

    private final Environment env;

    public LdapConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public LdapContextSource contextSource() {
        System.out.println("-----------------------------------");
        System.out.println(env.getRequiredProperty("fint.ldap.url"));
        System.out.println("-----------------------------------");
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(env.getRequiredProperty("fint.ldap.url"));
        contextSource.setUserDn(env.getRequiredProperty("fint.ldap.user"));
        contextSource.setPassword(env.getRequiredProperty("fint.ldap.password"));
        return contextSource;
    }

    @Bean
    public ContextSource poolingLdapContextSource() {

        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(60000);
        poolConfig.setMaxTotalPerKey(-1);
        poolConfig.setMaxIdlePerKey(-1);
        PooledContextSource pooledContextSource = new PooledContextSource(poolConfig);
        pooledContextSource.setContextSource(contextSource());
        pooledContextSource.setDirContextValidator(new DefaultDirContextValidator());

        return new TransactionAwareContextSourceProxy(pooledContextSource);
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(poolingLdapContextSource());
    }

}
