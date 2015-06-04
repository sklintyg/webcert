package se.inera.webcert.persistence.utkast.repository;

import org.joda.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.utkast.model.Omsandning;

import java.util.List;

public interface OmsandningRepositoryCustom extends OmsandningRepository {
    @Query("SELECT oms FROM Omsandning oms WHERE oms.gallringsdatum > :gallringsDatum AND oms.nastaForsok < :nastaForsok")
    List<Omsandning> findByGallringsdatumGreaterThanAndNastaForsokLessThan(@Param("gallringsDatum") LocalDateTime gallring, @Param("nastaForsok") LocalDateTime nastaForsok);
}
