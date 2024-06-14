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
import java.util.Collections;
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
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitQuestionsRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class ListCertificateQuestionsFromCSTest {

    private static final ArendeListItem ARENDE_LIST_ITEM = new ArendeListItem();
    private static final GetUnitQuestionsRequestDTO GET_UNIT_QUESTIONS_REQUEST_DTO = GetUnitQuestionsRequestDTO.builder().build();
    private static final QueryFragaSvarParameter QUERY_FRAGA_SVAR_PARAMETER = QueryFragaSvarParameter.builder().build();

    @InjectMocks
    ListCertificateQuestionsFromCS listCertificateQuestionsFromCS;

    @Mock
    CertificateServiceProfile certificateServiceProfile;

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    WebCertUserService webCertUserService;

    @Test
    void shouldReturnResponseWithNoValuesCSProfileIsNotActive() {
        final var expected = QueryFragaSvarResponse.builder()
            .results(Collections.emptyList())
            .totalCount(0)
            .build();

        final var response = listCertificateQuestionsFromCS.list(QUERY_FRAGA_SVAR_PARAMETER);
        assertEquals(expected, response);
    }

    @Test
    void shouldReturnListFromCSIfProfileIsActive() {
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(csIntegrationRequestFactory.getUnitQuestionsRequestDTO(any()))
            .thenReturn(GET_UNIT_QUESTIONS_REQUEST_DTO);
        when(csIntegrationService.listQuestionsForUnit(GET_UNIT_QUESTIONS_REQUEST_DTO))
            .thenReturn(List.of(ARENDE_LIST_ITEM));
        final var user = mock(WebCertUser.class);
        when(webCertUserService.getUser()).thenReturn(user);

        final var response = listCertificateQuestionsFromCS.list(QUERY_FRAGA_SVAR_PARAMETER);

        assertEquals(List.of(ARENDE_LIST_ITEM), response.getResults());
    }

    @Nested
    class FilterConverter {

        @BeforeEach
        void setUp() {
            when(certificateServiceProfile.active())
                .thenReturn(true);
        }

        @Test
        void shouldConvertSignedById() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getHsaId(), argumentCaptor.getValue().getIssuedByStaffId());
        }

        @Test
        void shouldConvertUnitIdIfSet() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getEnhetId(), argumentCaptor.getValue().getIssuedOnUnitIds().get(0));
            assertEquals(1, argumentCaptor.getValue().getIssuedOnUnitIds().size());
        }

        @Test
        void shouldSetUnitFromUserServiceIfUnitIdIsNull() {
            final var user = mock(WebCertUser.class);
            final var expected = List.of("unitId1", "unitId2");
            when(webCertUserService.getUser()).thenReturn(user);
            when(user.getIdsOfSelectedVardenhet()).thenReturn(expected);

            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            queryFragaSvarParameter.setEnhetId(null);

            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(expected, argumentCaptor.getValue().getIssuedOnUnitIds());
        }

        @Test
        void shouldConvertChangedFrom() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getChangedFrom(), argumentCaptor.getValue().getSentDateFrom());
        }

        @Test
        void shouldConvertChangedTo() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getChangedTo(), argumentCaptor.getValue().getSentDateTo());
        }

        @Test
        void shouldConvertForwarded() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getVidarebefordrad(), argumentCaptor.getValue().getForwarded());
        }

        @Test
        void shouldConvertPatientID() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(queryFragaSvarParameter.getPatientPersonId(), argumentCaptor.getValue().getPatientId().getId());
        }

        @Test
        void shouldSetSenderToShowAllIfBothSendersAreNull() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(null, null, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(SHOW_ALL, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetSenderToFKIfFKIsTrue() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(true, false, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(FK, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetSenderToWCIfWCIsTrue() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(WC, argumentCaptor.getValue().getSenderType());
        }

        @Test
        void shouldSetQuestionStatusTypeToComplement() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "KOMPLETTERING_FRAN_VARDEN");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(COMPLEMENT, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToHandled() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "HANTERAD");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(HANDLED, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToNotHandled() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "ALLA_OHANTERADE");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(NOT_HANDLED, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToAnswer() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "SVAR_FRAN_VARDEN");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(ANSWER, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToReadAnswer() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "MARKERA_SOM_HANTERAD");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(READ_ANSWER, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToWait() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "SVAR_FRAN_FK");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(WAIT, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToShowAll() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, "ALLA");
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(QuestionStatusType.SHOW_ALL, argumentCaptor.getValue().getQuestionStatus());
        }

        @Test
        void shouldSetQuestionStatusTypeToShowAllIfNull() {
            final var queryFragaSvarParameter = buildQueryFragaSvarParameter(false, true, null);
            listCertificateQuestionsFromCS.list(queryFragaSvarParameter);
            final var argumentCaptor = ArgumentCaptor.forClass(MessageQueryCriteriaDTO.class);

            verify(csIntegrationRequestFactory).getUnitQuestionsRequestDTO(argumentCaptor.capture());
            assertEquals(QuestionStatusType.SHOW_ALL, argumentCaptor.getValue().getQuestionStatus());
        }
    }

    private QueryFragaSvarParameter buildQueryFragaSvarParameter(Boolean fromFK, Boolean fromWC, String vantarPa) {
        return QueryFragaSvarParameter.builder()
            .questionFromWC(fromWC)
            .questionFromFK(fromFK)
            .vantarPa(vantarPa)
            .hsaId("hsaId")
            .enhetId("enhetId")
            .changedFrom(LocalDateTime.now())
            .changedTo(LocalDateTime.now())
            .patientPersonId("201212121212")
            .vidarebefordrad(true)
            .build();
    }
}
