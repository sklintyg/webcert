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

import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.ANSWER;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.COMPLEMENT;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.HANDLED;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.NOT_HANDLED;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.READ_ANSWER;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.SHOW_ALL;
import static se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType.WAIT;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdType;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionStatusType;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
@RequiredArgsConstructor
public class ListCertificateQuestionsFromCS {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;
    private final WebCertUserService webCertUserService;

    public QueryFragaSvarResponse list(QueryFragaSvarParameter queryFragaSvarParameter) {
        if (!certificateServiceProfile.active()) {
            return QueryFragaSvarResponse.builder()
                .results(Collections.emptyList())
                .totalCount(0)
                .build();
        }
        final var listFromCS = csIntegrationService.listQuestionsForUnit(
            csIntegrationRequestFactory.getUnitQuestionsRequestDTO(convertFilter(queryFragaSvarParameter)));

        return QueryFragaSvarResponse.builder()
            .results(listFromCS)
            .totalCount(listFromCS.size())
            .build();
    }

    private MessageQueryCriteriaDTO convertFilter(QueryFragaSvarParameter queryFragaSvarParameter) {
        return MessageQueryCriteriaDTO.builder()
            .issuedByStaffId(queryFragaSvarParameter.getHsaId())
            .forwarded(queryFragaSvarParameter.getVidarebefordrad())
            .sentDateFrom(queryFragaSvarParameter.getChangedFrom())
            .sentDateTo(queryFragaSvarParameter.getChangedTo())
            .issuedOnUnitIds(queryFragaSvarParameter.getEnhetId() != null ? List.of(queryFragaSvarParameter.getEnhetId())
                : webCertUserService.getUser().getIdsOfSelectedVardenhet())
            .patientId(convertPersonId(queryFragaSvarParameter.getPatientPersonId()))
            .senderType(convertSenderType(queryFragaSvarParameter))
            .questionStatus(convertStatus(queryFragaSvarParameter))
            .build();
    }

    private QuestionSenderType convertSenderType(QueryFragaSvarParameter queryFragaSvarParameter) {
        if (queryFragaSvarParameter.getQuestionFromFK() == null && queryFragaSvarParameter.getQuestionFromWC() == null) {
            return QuestionSenderType.SHOW_ALL;
        }
        return Boolean.TRUE.equals(queryFragaSvarParameter.getQuestionFromWC()) ? QuestionSenderType.WC : QuestionSenderType.FK;
    }

    private QuestionStatusType convertStatus(QueryFragaSvarParameter queryFragaSvarParameter) {
        if (queryFragaSvarParameter.getVantarPa() == null) {
            return SHOW_ALL;
        }
        switch (queryFragaSvarParameter.getVantarPa()) {
            case "HANTERAD":
                return HANDLED;
            case "ALLA_OHANTERADE":
                return NOT_HANDLED;
            case "KOMPLETTERING_FRAN_VARDEN":
                return COMPLEMENT;
            case "SVAR_FRAN_VARDEN":
                return ANSWER;
            case "MARKERA_SOM_HANTERAD":
                return READ_ANSWER;
            case "SVAR_FRAN_FK":
                return WAIT;
            case "ALLA":
            default:
                return SHOW_ALL;
        }
    }

    private PersonIdDTO convertPersonId(String patientId) {
        return patientId == null || patientId.isBlank() ? null
            : PersonIdDTO.builder()
                .id(patientId)
                .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                .build();
    }
}
