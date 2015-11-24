package se.inera.intyg.webcert.persistence.utkast.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@Transactional(value = "jpaTransactionManager", readOnly = true)
public interface UtkastFilteredRepositoryCustom {

    List<Utkast> filterIntyg(UtkastFilter filter);

    int countFilterIntyg(UtkastFilter filter);
}
