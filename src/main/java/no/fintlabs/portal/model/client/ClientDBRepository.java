package no.fintlabs.portal.model.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.naming.ldap.LdapName;


@Repository
public interface ClientDBRepository extends JpaRepository<Client, LdapName> {

    void deleteByName(String name);
}
