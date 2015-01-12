package se.inera.webcert.persistence.utkast.repository;

import java.util.List;

import se.inera.webcert.persistence.utkast.model.Utkast;

public interface UtkastFilteredRepositoryCustom {

    List<Utkast> filterIntyg(UtkastFilter filter);

    int countFilterIntyg(UtkastFilter filter);
}
