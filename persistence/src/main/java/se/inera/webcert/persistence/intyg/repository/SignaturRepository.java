package se.inera.webcert.persistence.intyg.repository;

import org.springframework.data.repository.CrudRepository;
import se.inera.webcert.persistence.intyg.model.Signatur;

public interface SignaturRepository extends CrudRepository<Signatur, String> {
}
