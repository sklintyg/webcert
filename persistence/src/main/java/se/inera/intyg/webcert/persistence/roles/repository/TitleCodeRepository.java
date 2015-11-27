package se.inera.intyg.webcert.persistence.roles.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.intyg.webcert.persistence.roles.model.TitleCode;

/**
 * Created by Magnus Ekstrand on 28/08/15.
 */
public interface TitleCodeRepository extends JpaRepository<TitleCode, Long> {

    TitleCode findByTitleCodeAndGroupPrescriptionCode(String titleCode, String groupPrescriptionCode);

    @Override
    void delete(TitleCode titleCode);

}
