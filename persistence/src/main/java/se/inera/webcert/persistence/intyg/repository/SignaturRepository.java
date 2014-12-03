package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.intyg.model.Signatur;

public interface SignaturRepository extends CrudRepository<Signatur, Long> {
    
    @Query("SELECT s from Signatur s WHERE s.intygId = :intygsId")
    List<Signatur> findSignaturerForIntyg(@Param("intygsId") String intygsId);
    
}
