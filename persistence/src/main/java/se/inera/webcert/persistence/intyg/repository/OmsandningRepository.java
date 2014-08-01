package se.inera.webcert.persistence.intyg.repository;

import org.springframework.data.repository.CrudRepository;
import se.inera.webcert.persistence.intyg.model.Omsandning;

public interface OmsandningRepository extends CrudRepository<Omsandning, Long> {
}
