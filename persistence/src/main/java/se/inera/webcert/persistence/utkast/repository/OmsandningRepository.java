package se.inera.webcert.persistence.utkast.repository;

import org.springframework.data.repository.CrudRepository;

import se.inera.webcert.persistence.utkast.model.Omsandning;

public interface OmsandningRepository extends CrudRepository<Omsandning, Long> {
}
