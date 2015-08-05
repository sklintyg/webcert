package se.inera.webcert.service.privatlakaravtal;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
public interface AvtalService {

    boolean userHasApprovedLatestAvtal(String userId);
    Avtal getLatestAvtal();
    void approveLatestAvtal(String userId);

}
