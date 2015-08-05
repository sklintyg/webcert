package se.inera.webcert.persistence.privatlakaravtal.repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Transactional(value = "jpaTransactionManager", readOnly = true)
public interface GodkantAvtalRepositoryCustom {

    void approveAvtal(String userId, Integer avtalVersion);

    boolean userHasApprovedAvtal(String userId, Integer avtalVersion);

    void removeUserApprovement(String userId, Integer avtalVersion);

    void removeAllUserApprovments(String userId);

}
