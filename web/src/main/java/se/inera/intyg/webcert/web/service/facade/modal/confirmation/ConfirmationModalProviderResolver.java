/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.modal.confirmation;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;

public class ConfirmationModalProviderResolver {

    private ConfirmationModalProviderResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static ConfirmationModalProvider get(String type, CertificateStatus status, String origin, boolean isCreatedFromList) {
        final var isIntegratedOrigin = origin.equals("DJUPINTEGRATION");
        final var isValid = isIntegratedOrigin ? isValidForIntegratedOrigin() : isValidForNormalOrigin(isCreatedFromList);

        if (!isValid) {
            return null;
        }

        if (status != CertificateStatus.UNSIGNED) {
            return null;
        }

        if (type.equals(DbModuleEntryPoint.MODULE_ID)) {
            return new DbConfirmationModalProvider();
        }

        return null;
    }

    private static boolean isValidForIntegratedOrigin() {
        return true;
    }

    private static boolean isValidForNormalOrigin(boolean isCreatedFromList) {
        return isCreatedFromList;
    }

}
