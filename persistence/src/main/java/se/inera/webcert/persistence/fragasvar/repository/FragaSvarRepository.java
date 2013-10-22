package se.inera.webcert.persistence.fragasvar.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import java.util.List;

public interface FragaSvarRepository extends CrudRepository<FragaSvar, Long>, FragaSvarRepositoryCustom {

}
