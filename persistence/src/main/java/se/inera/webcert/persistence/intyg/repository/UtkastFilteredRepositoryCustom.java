package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import se.inera.webcert.persistence.intyg.model.Utkast;

public interface UtkastFilteredRepositoryCustom {

    List<Utkast> filterIntyg(UtkastFilter filter);

    int countFilterIntyg(UtkastFilter filter);
}
