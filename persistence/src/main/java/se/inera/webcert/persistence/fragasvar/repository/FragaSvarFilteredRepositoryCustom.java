package se.inera.webcert.persistence.fragasvar.repository;

import org.springframework.data.domain.Pageable;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import java.util.List;

/**
 * Created by pehr on 10/21/13.
 */
public interface FragaSvarFilteredRepositoryCustom {

    List<FragaSvar> filterFragaSvar(FragaSvarFilter filter);

    public List<FragaSvar> filterFragaSvar(FragaSvarFilter filter, Pageable pages);
}
