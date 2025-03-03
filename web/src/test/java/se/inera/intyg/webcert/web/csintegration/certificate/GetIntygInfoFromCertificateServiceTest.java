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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class GetIntygInfoFromCertificateServiceTest {

    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CertificateToIntygInfoConverter certificateToIntygInfoConverter;
    @InjectMocks
    GetIntygInfoFromCertificateService getIntygInfoFromCertificateService;

    private static final String CERTIFICATE_ID = "ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final WcIntygInfo INTYG_INFO = new WcIntygInfo();
    private static final List<Question> QUESTIONS = List.of(Question.builder().build());

    @Test
    void shouldReturnOptionalEmptyIfCertificateDoesNotExistInCS() {
        assertTrue(
            getIntygInfoFromCertificateService.getIntygInfo(CERTIFICATE_ID).isEmpty()
        );
    }

    @Nested
    class CertificateServiceHasCertificate {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID))
                .thenReturn(true);
            when(csIntegrationService.getInternalCertificate(CERTIFICATE_ID))
                .thenReturn(CERTIFICATE);
            when(csIntegrationService.getQuestions(CERTIFICATE_ID))
                .thenReturn(QUESTIONS);
            when(certificateToIntygInfoConverter.convert(CERTIFICATE, QUESTIONS))
                .thenReturn(INTYG_INFO);
        }

        @Test
        void shouldReturnCertificate() {
            assertEquals(INTYG_INFO,
                getIntygInfoFromCertificateService.getIntygInfo(CERTIFICATE_ID).get()
            );
        }
    }

}