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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.fmb.FmbDiagnosInformationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.MaximalSjukskrivningstidResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;

@ExtendWith(MockitoExtension.class)
public class ValidateSickLeavePeriodFacadeServiceImplTest {

    @Mock
    private FmbDiagnosInformationService fmbDiagnosInformationService;

    @InjectMocks
    private ValidateSickLeavePeriodFacadeServiceImpl validateSickLeavePeriodFacadeService;

    private final static String PERSON_ID = "19121212-1212";
    private ValidateSickLeavePeriodRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new ValidateSickLeavePeriodRequestDTO();
        List<CertificateDataValueDateRange> dateRangeList = new ArrayList();
        String[] codes = {
            "A201"
        };
        dateRangeList.add(CertificateDataValueDateRange.builder()
            .from(LocalDate.now())
            .to(LocalDate.now().plusDays(2))
            .id("EN_FJARDEDEL")
            .build());
        request.setPersonId(PERSON_ID);
        request.setDateRangeList(CertificateDataValueDateRangeList.builder()
            .list(dateRangeList)
            .build());
        request.setIcd10Codes(codes);
    }

    @Test
    void shallValidateSickLeavePeriod() {
        final var expectedResponse = new MaximalSjukskrivningstidResponse();
        expectedResponse.setOverskriderRekommenderadSjukskrivningstid(false);
        Mockito.doReturn(expectedResponse).when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var actualResponse = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        verify(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());
        assertNotNull(actualResponse);
    }

    @Test
    void shallNotReturnWarningIfRecommendationIsNotOvercome() {
        final var response = new MaximalSjukskrivningstidResponse();
        response.setOverskriderRekommenderadSjukskrivningstid(false);
        Mockito.doReturn(response).when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var warning = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        assertTrue(warning.length() == 0);
    }

    @Test
    void shallReturnWarningIfRecommendationIsOvercome() {
        final var response = new MaximalSjukskrivningstidResponse();
        response.setOverskriderRekommenderadSjukskrivningstid(true);
        Mockito.doReturn(response).when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var warning = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        assertTrue(warning.length() > 0);
    }

    @Test
    void shallReturnSpecialStringIfOnlyCurrentCertificateInSickleave() {
        final var response = new MaximalSjukskrivningstidResponse();
        response.setOverskriderRekommenderadSjukskrivningstid(true);
        response.setTotalSjukskrivningstid(3);
        Mockito.doReturn(response).when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var warning = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        assertTrue(warning.length() > 0);
        assertTrue(warning.contains("Den föreslagna sjukskrivningsperioden"));
    }

    @Test
    void shallReturnSpecialStringIfNotOnlyCurrentCertificateInSickleave() {
        final var response = new MaximalSjukskrivningstidResponse();
        response.setOverskriderRekommenderadSjukskrivningstid(true);
        response.setTotalSjukskrivningstid(10);
        Mockito.doReturn(response).when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var warning = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        assertTrue(warning.length() > 0);
        assertTrue(warning.contains("Den totala sjukskrivningsperioden är"));
    }

    @Test
    void shallReturnErrorMessageIfExceptionIsThrown() {
        Mockito.doThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, "Failed"))
            .when(fmbDiagnosInformationService).validateSjukskrivningtidForPatient(any());

        final var warning = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(request);
        assertTrue(warning.length() > 0);
        assertTrue(warning.contains(
            "På grund av ett tekniskt fel kan vi just nu inte räkna ut om patienten överskrider FMB:s rekommenderade sjukskrivningslängd."));
    }
}
