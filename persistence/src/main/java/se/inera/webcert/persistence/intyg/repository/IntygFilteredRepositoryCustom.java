package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import se.inera.webcert.persistence.intyg.model.Intyg;

public interface IntygFilteredRepositoryCustom {

    List<Intyg> filterIntyg(IntygFilter filter);
}
