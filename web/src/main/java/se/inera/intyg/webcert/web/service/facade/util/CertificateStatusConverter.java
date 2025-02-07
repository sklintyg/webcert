/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.util;

import java.util.List;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public abstract class CertificateStatusConverter {

    public static CertificateStatus getStatus(boolean isRevoked, UtkastStatus status) {
        if (isRevoked) {
            if (status == UtkastStatus.DRAFT_LOCKED) {
                return CertificateStatus.LOCKED_REVOKED;
            } else {
                return CertificateStatus.REVOKED;
            }
        }

        switch (status) {
            case SIGNED:
                return CertificateStatus.SIGNED;
            case DRAFT_LOCKED:
                return CertificateStatus.LOCKED;
            case DRAFT_COMPLETE:
            case DRAFT_INCOMPLETE:
                return CertificateStatus.UNSIGNED;
            default:
                throw new IllegalArgumentException("Cannot map the status: " + status);
        }
    }

    public static CertificateStatus getStatus(List<Status> statuses) {
        if (statuses.stream().anyMatch(status -> status.getType() == CertificateState.CANCELLED)) {
            return CertificateStatus.REVOKED;
        }
        return CertificateStatus.SIGNED;
    }

    public static boolean isRevoked(Utkast certificate) {
        return certificate.getAterkalladDatum() != null;
    }
}
