package se.inera.intyg.webcert.persistence.handelse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

import java.util.List;

public interface HandelseRepository extends JpaRepository<Handelse, Long> {

    List<Handelse> findByIntygsId(String intygsId);
}
