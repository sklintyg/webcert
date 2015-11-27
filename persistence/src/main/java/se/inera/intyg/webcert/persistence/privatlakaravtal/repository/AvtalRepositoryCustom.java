package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Transactional(value = "jpaTransactionManager", readOnly = false)
public interface AvtalRepositoryCustom {
    Integer getLatestAvtalVersion();
}
