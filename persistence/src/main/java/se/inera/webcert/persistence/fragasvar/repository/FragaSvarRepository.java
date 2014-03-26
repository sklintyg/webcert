package se.inera.webcert.persistence.fragasvar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public interface FragaSvarRepository extends JpaRepository<FragaSvar, Long>, FragaSvarRepositoryCustom {

}
