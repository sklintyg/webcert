package se.inera.webcert.persistence.utkast.repository;

import org.springframework.data.repository.CrudRepository;

import se.inera.webcert.persistence.utkast.model.Utkast;

public interface UtkastRepository extends CrudRepository<Utkast, String>, UtkastRepositoryCustom {

}
