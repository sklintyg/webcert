package se.inera.webcert.persistence.utkast.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.utkast.model.Utkast;

@Transactional(readOnly = true)
public interface UtkastFilteredRepositoryCustom {

    List<Utkast> filterIntyg(UtkastFilter filter);

    int countFilterIntyg(UtkastFilter filter);
}
