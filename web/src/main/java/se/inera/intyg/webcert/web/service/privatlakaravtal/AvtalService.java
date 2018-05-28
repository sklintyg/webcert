/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.privatlakaravtal;

import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;

import java.util.Optional;

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
    Optional<Avtal> getLatestAvtal();

    /**
     * Stores approval for the specicfied user for the currently latest avtal.
     */
    void approveLatestAvtal(String userId, String personId);

    /**
     * Removes all approvals of terms for the specified user.
     */
    void removeApproval(String userId);
}
