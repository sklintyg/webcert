package se.inera.intyg.webcert.persistence.utkast.repository;

import org.springframework.data.repository.CrudRepository;

import se.inera.intyg.webcert.persistence.utkast.model.ScheduleratJobb;

public interface ScheduleratJobbRepository extends CrudRepository<ScheduleratJobb, String> {
}
