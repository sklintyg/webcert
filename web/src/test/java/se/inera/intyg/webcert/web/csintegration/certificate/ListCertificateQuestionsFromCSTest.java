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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType.FK;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType.SHOW_ALL;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType.WC;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.ANSWER;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.COMPLEMENT;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.HANDLED;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.NOT_HANDLED;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.READ_ANSWER;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.WAIT;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.QuestionStatusFilter;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageQueryCriteriaDTO;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class ListCertificateQuestionsFromCSTest {

    private static final ArendeListItem ARENDE_LIST_ITEM = new ArendeListItem();
    private static final ArendeListItem ARENDE_LIST_NOT_INCLUDED_IN_STATUS = new ArendeListItem();
    private static final GetUnitQuestionsRequestDTO GET_UNIT_QUESTIONS_REQUEST_DTO = GetUnitQuestionsRequestDTO.builder().build();
    private static final String HSA_ID = "hsaId";
    private static QueryFragaSvarParameter queryFragaSvarParameter;

    @InjectMocks
    ListCertificateQuestionsFromCS listCertificateQuestionsFromCS;
    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    QuestionStatusFilter questionStatusFilter;

    @Mock
    WebCertUserService webCertUserService;

    @BeforeEach
    void setUp() {
        queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
            "SVAR_FRAN_VARDEN", HSA_ID);
    }

    @BeforeEach
    void setup() {
        when(csIntegrationRequestFactory.getUnitQuestionsRequestDTO(any()))
            .thenReturn(GET_UNIT_QUESTIONS_REQUEST_DTO);
        final var user = mock(WebCertUser.class);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    void shouldFilterNotIncludedInStatus() {
        when(questionStatusFilter.validate(ARENDE_LIST_NOT_INCLUDED_IN_STATUS, ANSWER))
            .thenReturn(false);
        when(questionStatusFilter.validate(ARENDE_LIST_ITEM, ANSWER))
            .thenReturn(true);
        when(csIntegrationService.listQuestionsForUnit(GET_UNIT_QUESTIONS_REQUEST_DTO))
            .thenReturn(List.of(ARENDE_LIST_ITEM, ARENDE_LIST_NOT_INCLUDED_IN_STATUS));

        final var response = listCertificateQuestionsFromCS.list(queryFragaSvarParameter);

        assertEquals(List.of(ARENDE_LIST_ITEM), response.getResults());
    }

    @Test
    void shouldReturnTotalCountValueBeforeFiltering() {
        when(questionStatusFilter.validate(ARENDE_LIST_NOT_INCLUDED_IN_STATUS, ANSWER))
            .thenReturn(false);
        when(questionStatusFilter.validate(ARENDE_LIST_ITEM, ANSWER))
            .thenReturn(true);
        when(csIntegrationService.listQuestionsForUnit(GET_UNIT_QUESTIONS_REQUEST_DTO))
            .thenReturn(List.of(ARENDE_LIST_ITEM, ARENDE_LIST_NOT_INCLUDED_IN_STATUS));

        final var response = listCertificateQuestionsFromCS.list(queryFragaSvarParameter);

        assertEquals(1, response.getTotalCount());
    }

    @Nested
    class FilterConverter {

        @BeforeEach
        void setUp() {
            when(csIntegrationService.listQuestionsForUnit(GET_UNIT_QUESTIONS_REQUEST_DTO))
                .thenReturn(List.of(ARENDE_LIST_ITEM));
        }

        @Test
        void shouldConvertSignedById() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getHsaId(),
                argumentCaptor.getValue().getIssuedByStaffId());
        }

        @Test
        void shouldExcludeSignedByIdIfEmpty() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture()
            );
            assertNull(argumentCaptor.getValue().getIssuedByStaffId());
        }

        @Test
        void shouldExcludeSignedByIdIfNull() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", null);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture()
            );
            assertNull(argumentCaptor.getValue().getIssuedByStaffId());
        }

        @Test
        void shouldConvertUnitIdIfSet() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            queryFragaSvarParameter.setEnhetId("unitId");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getEnhetId(),
                argumentCaptor.getValue().getIssuedOnUnitIds().getFirst());
            assertEquals(1, argumentCaptor.getValue().getIssuedOnUnitIds().size());
        }

        @Test
        void shouldSetUnitFromUserServiceIfUnitIdIsNull() {
            final var user = mock(WebCertUser.class);
            final var expected = List.of("unitId1", "unitId2");
            when(webCertUserService.getUser()).thenReturn(user);
            when(user.getIdsOfSelectedVardenhet()).thenReturn(expected);

            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            queryFragaSvarParameter.setEnhetId(null);

            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(expected, argumentCaptor.getValue().getIssuedOnUnitIds());
        }

        @Test
        void shouldConvertChangedFrom() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getChangedFrom(),
                argumentCaptor.getValue().getSentDateFrom());
        }

        @Test
        void shouldConvertChangedTo() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getChangedTo(),
                argumentCaptor.getValue().getSentDateTo());
        }

        @Test
        void shouldConvertForwarded() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getVidarebefordrad(),
                argumentCaptor.getValue().getForwarded());
        }

        @Test
        void shouldConvertPatientID() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getPatientPersonId(),
                argumentCaptor.getValue().getPatientId().getId());
        }

        @Test
        void shouldSetSenderToShowAllIfBothSendersAreFalse() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(SHOW_ALL, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetSenderToFKIfFKIsTrue() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(FK, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetSenderToWCIfWCIsTrue() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(
                argumentCaptor.capture());
            assertEquals(WC, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetQuestionStatusTypeToComplement() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "KOMPLETTERING_FRAN_VARDEN", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(COMPLEMENT, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToHandled() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "HANTERAD", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(HANDLED, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToNotHandled() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "ALLA_OHANTERADE", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(NOT_HANDLED, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToAnswer() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "SVAR_FRAN_VARDEN", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(ANSWER, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToReadAnswer() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "MARKERA_SOM_HANTERAD", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(READ_ANSWER, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToWait() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "SVAR_FRAN_FK", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(WAIT, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToShowAll() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true,
                "ALLA", HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(QuestionStatusType.SHOW_ALL, argumentCaptor.getValue());
        }

        @Test
        void shouldSetQuestionStatusTypeToShowAllIfNull() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, null, HSA_ID);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(QuestionStatusType.class);

            verify(questionStatusFilter).validate(any(), argumentCaptor.capture());
            assertEquals(QuestionStatusType.SHOW_ALL, argumentCaptor.getValue());
        }
    }

    private QueryFragaSvarParameter buildQueryFragaSvarParameter(Boolean fromFK, Boolean fromWC,
        String vantarPa, String hsaId) {
        return QueryFragaSvarParameter.builder()
            .questionFromWC(fromWC)
            .questionFromFK(fromFK)
            .vantarPa(vantarPa)
            .hsaId(hsaId)
            .enhetId(null)
            .changedFrom(LocalDateTime.now())
            .changedTo(LocalDateTime.now())
            .patientPersonId("201212121212")
            .vidarebefordrad(true)
            .build();
    }
}
