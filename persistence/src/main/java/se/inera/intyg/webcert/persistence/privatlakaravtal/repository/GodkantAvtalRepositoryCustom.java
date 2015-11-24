package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Transactional(value = "jpaTransactionManager", readOnly = false)
public interface GodkantAvtalRepositoryCustom {

    void approveAvtal(String hsaId, Integer avtalVersion);

    boolean userHasApprovedAvtal(String hsaId, Integer avtalVersion);

    void removeUserApprovement(String hsaId, Integer avtalVersion);

    void removeAllUserApprovments(String hsaId);

}
