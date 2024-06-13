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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class ListCertificateQuestionsFromCSTest {

    private static final String PATIENT_ID = "191212121212";
    private static final ArendeListItem ARENDE_LIST_ITEM = new ArendeListItem();
    private static final GetUnitQuestionsRequestDTO GET_UNIT_QUESTIONS_REQUEST_DTO = GetUnitQuestionsRequestDTO.builder().build();
    @InjectMocks
    ListCertificateQuestionsFromCS listCertificateQuestionsFromCS;

    @Mock
    CertificateServiceProfile certificateServiceProfile;

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Test
    void shouldReturnResponseWithNoValuesCSProfileIsNotActive() {
        final var expected = QueryFragaSvarResponse.builder()
            .results(Collections.emptyList())
            .totalCount(0)
            .build();

        final var response = listCertificateQuestionsFromCS.list(PATIENT_ID);
        assertEquals(expected, response);
    }

    @Test
    void shouldReturnListFromCSIfProfileIsActive() {
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(csIntegrationRequestFactory.getUnitQuestionsRequestDTO(PATIENT_ID))
            .thenReturn(GET_UNIT_QUESTIONS_REQUEST_DTO);
        when(csIntegrationService.listQuestionsForUnit(GET_UNIT_QUESTIONS_REQUEST_DTO))
            .thenReturn(List.of(ARENDE_LIST_ITEM));

        final var response = listCertificateQuestionsFromCS.list(PATIENT_ID);

        assertEquals(List.of(ARENDE_LIST_ITEM), response.getResults());
    }
}
