package se.inera.intyg.webcert.persistence.fragasvar.repository;

import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;

import java.util.List;

/**
 * Created by pehr on 10/21/13.
 */
public interface FragaSvarFilteredRepositoryCustom {

    List<FragaSvar> filterFragaSvar(FragaSvarFilter filter);

    int filterCountFragaSvar(FragaSvarFilter filter);
}
