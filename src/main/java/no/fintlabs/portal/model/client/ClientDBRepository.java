package no.fintlabs.portal.model.client;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.naming.Name;


public interface ClientDBRepository extends JpaRepository<Client, Name> {
}
