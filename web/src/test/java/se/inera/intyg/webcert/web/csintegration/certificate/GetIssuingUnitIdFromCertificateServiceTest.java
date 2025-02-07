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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class GetIssuingUnitIdFromCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String EXPECTED_UNIT_ID = "expectedUnitId";
    @Mock
    CSIntegrationService csIntegrationService;
    @InjectMocks
    GetIssuingUnitIdFromCertificateService getIssuingUnitIdFromCertificateService;

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService() {
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);

        final var actualResult = getIssuingUnitIdFromCertificateService.get(CERTIFICATE_ID);
        assertNull(actualResult);
    }

    @Test
    void shallReturnUnitIdFromCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .unit(
                    Unit.builder()
                        .unitId(EXPECTED_UNIT_ID)
                        .build()
                )
                .build()
        );
        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(certificate).when(csIntegrationService).getInternalCertificate(CERTIFICATE_ID);

        final var actualResult = getIssuingUnitIdFromCertificateService.get(CERTIFICATE_ID);
        assertEquals(EXPECTED_UNIT_ID, actualResult);
    }
}
