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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.certificate.RenewLegacyCertificateFromCertificateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;

class RenewCertificateAggregatorTest {

    private static final String ID = "ID";
    private static final String NEW_ID = "NEW_ID";
    private static final String TYPE = "TYPE";
    private Certificate certificate;

    RenewCertificateFacadeService renewCertificateFromWC;
    RenewCertificateFacadeService renewCertificateFromCS;
    CertificateServiceProfile certificateServiceProfile;
    RenewCertificateFacadeService aggregator;
    RenewLegacyCertificateFromCertificateService renewLegacyCertificateFromCertificateService;
    CSIntegrationService csIntegrationService;
    GetCertificateAggregator getCertificateAggregator;

    @BeforeEach
    void setup() {
        renewCertificateFromWC = mock(RenewCertificateFacadeService.class);
        renewCertificateFromCS = mock(RenewCertificateFacadeService.class);
        certificateServiceProfile = mock(CertificateServiceProfile.class);
        renewLegacyCertificateFromCertificateService = mock(RenewLegacyCertificateFromCertificateService.class);
        csIntegrationService = mock(CSIntegrationService.class);
        getCertificateAggregator = mock(GetCertificateAggregator.class);

        certificate = mock(Certificate.class);
        when(certificate.getMetadata())
            .thenReturn(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build());

        aggregator = new RenewCertificateAggregator(
            renewCertificateFromWC,
            renewCertificateFromCS,
            certificateServiceProfile,
            csIntegrationService,
            getCertificateAggregator,
            renewLegacyCertificateFromCertificateService
        );
    }

    @Test
    void shouldRenewFromWebcertIfProfileIsInactive() {
        aggregator.renewCertificate(ID);

        Mockito.verify(renewCertificateFromWC).renewCertificate(ID);
    }

    @Nested
    class ActiveProfile {

        @BeforeEach
        void setup() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldRenewFromCSIfProfileIsActiveAndCertificateExistsInCS() {
            when(renewCertificateFromCS.renewCertificate(ID))
                .thenReturn(NEW_ID);
            aggregator.renewCertificate(ID);

            Mockito.verify(renewCertificateFromWC, times(0)).renewCertificate(ID);
        }

        @Test
        void shouldRenewFromWCIfCertificateAndTypeDoesNotExistInCS() {
            when(getCertificateAggregator.getCertificate(ID, false, true))
                .thenReturn(certificate);
            when(csIntegrationService.certificateTypeExists(TYPE))
                .thenReturn(Optional.empty());
            aggregator.renewCertificate(ID);

            Mockito.verify(renewCertificateFromWC, times(1)).renewCertificate(ID);
        }

        @Test
        void shouldRenewFromLegacyServiceCertificateDoesNotExistInCSButTypeDoes() {
            when(getCertificateAggregator.getCertificate(ID, false, true))
                .thenReturn(certificate);
            final var modelIdDTO = CertificateModelIdDTO.builder().build();
            when(csIntegrationService.certificateTypeExists(TYPE))
                .thenReturn(Optional.of(modelIdDTO));
            aggregator.renewCertificate(ID);

            Mockito.verify(renewLegacyCertificateFromCertificateService, times(1)).renewCertificate(certificate, modelIdDTO);
        }
    }
}