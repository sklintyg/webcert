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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.getStatus;
import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.isRevoked;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

class CertificateStatusConverterTest {

    @Nested
    class GetStatusFromUtkastStatus {

        @Test
        void shallReturnRevokedIfCertificateSignedIsRevoked() {
            assertEquals(CertificateStatus.REVOKED, getStatus(true, UtkastStatus.SIGNED));
        }

        @Test
        void shallReturnLockedRevokedIfDraftLockedIsRevoked() {
            assertEquals(CertificateStatus.LOCKED_REVOKED, getStatus(true, UtkastStatus.DRAFT_LOCKED));
        }

        @Test
        void shallReturnLockedIfDraftIsLocked() {
            assertEquals(CertificateStatus.LOCKED, getStatus(false, UtkastStatus.DRAFT_LOCKED));
        }

        @Test
        void shallReturnSignedIfCertificateIsSigned() {
            assertEquals(CertificateStatus.SIGNED, getStatus(false, UtkastStatus.SIGNED));
        }

        @Test
        void shallReturnUnsignedIfDraftIsInComplete() {
            assertEquals(CertificateStatus.UNSIGNED, getStatus(false, UtkastStatus.DRAFT_INCOMPLETE));
        }

        @Test
        void shallReturnUnsignedIfDraftIsComplete() {
            assertEquals(CertificateStatus.UNSIGNED, getStatus(false, UtkastStatus.DRAFT_COMPLETE));
        }
    }

    @Nested
    class GetStatusFromIntygStatusList {

        private final Status RECIEVED_STATUS = new Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now());
        private final Status SENT_STATUS = new Status(CertificateState.SENT, "FK", LocalDateTime.now());
        private final Status CANCELLED_STATUS = new Status(CertificateState.CANCELLED, "HSVARD", LocalDateTime.now());

        @Test
        void shallReturnSignedIfListMissingCancelled() {
            assertEquals(CertificateStatus.SIGNED, getStatus(Arrays.asList(RECIEVED_STATUS, SENT_STATUS)));
        }

        @Test
        void shallReturnRevokedIfListContainsCancelled() {
            assertEquals(CertificateStatus.REVOKED, getStatus(Arrays.asList(RECIEVED_STATUS, SENT_STATUS, CANCELLED_STATUS)));
        }
    }

    @Nested
    class IsRevoked {

        private final Utkast UTKAST = new Utkast();

        @Test
        void shallReturnFalseIfUtkastMissingAterkalladDatum() {
            UTKAST.setAterkalladDatum(null);
            assertEquals(false, isRevoked(UTKAST));
        }

        @Test
        void shallReturnTrueIfUtkastContainsAterkalladDatum() {
            UTKAST.setAterkalladDatum(LocalDateTime.now());
            assertEquals(true, isRevoked(UTKAST));
        }
    }
}
