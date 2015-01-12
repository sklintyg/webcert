package se.inera.webcert.persistence.intyg.repository;

import org.springframework.data.repository.CrudRepository;

import se.inera.webcert.persistence.intyg.model.Utkast;

public interface UtkastRepository extends CrudRepository<Utkast, String>, UtkastRepositoryCustom {

}
