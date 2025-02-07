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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepositoryCustom;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@ExtendWith(MockitoExtension.class)
class CertificateRelationsParentHelperImplTest {

    @Mock
    private UtkastRepositoryCustom utkastRepositoryCustom;

    @Mock
    private IntygService intygService;

    @InjectMocks
    private CertificateRelationsParentHelperImpl certificateRelationsParentHandler;

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";

    @Test
    void shallReturnNullIfCertificateDoesntExistsInWebcert() {
        doReturn(Collections.emptyList())
            .when(utkastRepositoryCustom).findParentRelationWhenParentMissing(CERTIFICATE_ID);

        assertNull(certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID));
    }

    @Test
    void shallReturnNullIfNoRelationExistsForTheCertificate() {
        final var webcertCertificateRelation = new WebcertCertificateRelation(null, null, null, null,
            false);
        doReturn(List.of(webcertCertificateRelation))
            .when(utkastRepositoryCustom).findParentRelationWhenParentMissing(CERTIFICATE_ID);

        assertNull(certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID));
    }

    @Nested
    class ParentInIntygstjanst {

        private final String parentCertificateId = "PARENT_CERTIFICATE_ID";
        private final RelationKod parentRelationCode = RelationKod.KOMPLT;
        private final LocalDateTime relationCreated = LocalDateTime.now();
        private IntygContentHolder certifificate;

        @BeforeEach
        void setUp() {
            final var webcertCertificateRelation = new WebcertCertificateRelation(
                parentCertificateId, parentRelationCode, relationCreated, null,
                false);

            doReturn(List.of(webcertCertificateRelation))
                .when(utkastRepositoryCustom).findParentRelationWhenParentMissing(CERTIFICATE_ID);

            certifificate = mock(IntygContentHolder.class);

            doReturn(certifificate)
                .when(intygService).fetchIntygDataForInternalUse(parentCertificateId, false);
        }

        @Test
        void shallReturnParentCertificateId() {
            assertEquals(parentCertificateId,
                certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).getIntygsId());
        }

        @Test
        void shallReturnParentRelationCode() {
            assertEquals(parentRelationCode,
                certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).getRelationKod());
        }

        @Test
        void shallReturnRelationCreated() {
            assertEquals(relationCreated,
                certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).getSkapad());
        }

        @Test
        void shallReturnStatusSignedAsCertificatesInITAreAlwaysSigned() {
            assertEquals(UtkastStatus.SIGNED,
                certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).getStatus());
        }

        @Test
        void shallReturnParentRelationRevokedStatusWhenFalse() {
            doReturn(false).when(certifificate).isRevoked();

            assertFalse(certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).isMakulerat());
        }

        @Test
        void shallReturnParentRelationRevokedStatusWhenTrue() {
            doReturn(true).when(certifificate).isRevoked();

            assertTrue(certificateRelationsParentHandler.getParentFromITIfExists(CERTIFICATE_ID).isMakulerat());
        }
    }
}
