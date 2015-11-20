package se.inera.intyg.webcert.web.service.privatlakaravtal;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
public interface AvtalService {
    /**
     * Returns true if the specified user has approved the latest avtal in the database.
     */
    boolean userHasApprovedLatestAvtal(String userId);

    /**
     * Returns the latest avtal stored in the database.
     */
    Avtal getLatestAvtal();

    /**
     * Stores approval for the specicfied user for the currently latest avtal.
     */
    void approveLatestAvtal(String userId, String personId);

    /**
     * Removes all approvals of terms for the specified user.
     */
    void removeApproval(String userId);
}
