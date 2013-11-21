package se.inera.webcert.persistence.intyg.repository;

import org.springframework.data.repository.CrudRepository;

import se.inera.webcert.persistence.intyg.model.Intyg;

public interface IntygRepository extends CrudRepository<Intyg, String>, IntygRepositoryCustom {

}
