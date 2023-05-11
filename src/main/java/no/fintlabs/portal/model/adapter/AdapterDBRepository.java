package no.fintlabs.portal.model.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.naming.ldap.LdapName;

@Repository
public interface AdapterDBRepository extends JpaRepository<Adapter, LdapName> {
    void deleteByName(String name);
}
