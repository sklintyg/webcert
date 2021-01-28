/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.access;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Service to check the current users right to access actions on a Locked Draft.
 */
public interface LockedDraftAccessService {

    /**
     * Check if the user is allowed to read a locked draft.
     *
     * @param certificateType The type of the certificate being checked.
     * @param careUnit The careUnit which the certificate belongs to.
     * @param patient The patient which the certificate belongs to.
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToRead(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to copy a locked draft.
     *
     * @param certificateType The type of the certificate being checked.
     * @param careUnit The careUnit which the certificate belongs to.
     * @param patient The patient which the certificate belongs to.
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowedToCopyLockedUtkast(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to invalidate a locked draft.
     *
     * @param certificateType The type of the certificate being checked.
     * @param careUnit The careUnit which the certificate belongs to.
     * @param patient The patient which the certificate belongs to.
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowedToInvalidateLockedUtkast(String certificateType, Vardenhet careUnit, Personnummer patient);

    /**
     * Check if the user is allowed to print a locked draft.
     *
     * @param certificateType The type of the certificate being checked.
     * @param careUnit The careUnit which the certificate belongs to.
     * @param patient The patient which the certificate belongs to.
     * @return AccessResult which contains the answer if the user is allowed or not.
     */
    AccessResult allowToPrint(String certificateType, Vardenhet careUnit, Personnummer patient);
}
