package se.inera.webcert.persistence.fragasvar.repository;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import java.util.List;

/**
 * Created by pehr on 10/21/13.
 */
public interface FragaSvarFilteredRepositoryCustom {

    List<FragaSvar> filterFragaSvar(FragaSvarFilter filter);

    List<FragaSvar> filterFragaSvar(FragaSvarFilter filter, int startPos, int size);

    int filterCountFragaSvar(FragaSvarFilter filter) ;
}
