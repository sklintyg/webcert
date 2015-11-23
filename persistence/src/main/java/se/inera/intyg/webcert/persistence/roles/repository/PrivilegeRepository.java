package se.inera.intyg.webcert.persistence.roles.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.intyg.webcert.persistence.roles.model.Privilege;

/**
 * Created by Magnus Ekstrand on 28/08/15.
 */
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(String name);

    @Override
    void delete(Privilege privilege);

}
