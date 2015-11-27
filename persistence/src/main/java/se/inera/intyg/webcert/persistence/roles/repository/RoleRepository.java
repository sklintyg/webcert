package se.inera.intyg.webcert.persistence.roles.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.intyg.webcert.persistence.roles.model.Role;

/**
 *
 * Created by Magnus Ekstrand on 2015-08-27.
 *
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

    @Override
    void delete(Role role);

}
