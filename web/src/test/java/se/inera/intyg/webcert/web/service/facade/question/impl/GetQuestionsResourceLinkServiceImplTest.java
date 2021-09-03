/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.question.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

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
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetQuestionsResourceLinkServiceImplTest {

    @Mock
    private GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Mock
    private ArendeService arendeService;

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @InjectMocks
    private GetQuestionsResourceLinkServiceImpl getQuestionsResourceLinkService;

    private Question question;
    private ResourceLinkDTO resourceLinkDTO;
    private AccessEvaluationParameters accessEvaluationParameters;

    @Test
    void shallIncludeNoLinksIfQuestionDraft() {
        question = Question.builder().id("questionId").build();
        final var actualLinks = getQuestionsResourceLinkService.get(question);
        assertTrue(actualLinks.size() == 0, "Links should be empty");
    }

    @Test
    void shallIncludeNoLinksIfListHaveQuestionDraft() {
        question = Question.builder().id("questionId").build();
        final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));
        assertTrue(actualLinks.get(question).size() == 0, "Links should be empty");
    }

    @Nested
    class SingleQuestion {

        @BeforeEach
        void setUp() {
            question = Question.builder()
                .id("questionId")
                .sent(LocalDateTime.now())
                .build();
            resourceLinkDTO = new ResourceLinkDTO();

            doReturn(Collections.singletonList(resourceLinkDTO))
                .when(getQuestionsAvailableFunctionsService)
                .get(question);

            final var arende = new Arende();
            arende.setEnhetId("unitId");
            arende.setPatientPersonId("191212121212");
            doReturn(arende)
                .when(arendeService)
                .getArende(question.getId());

            final var careProviderId = "careProviderId";
            doReturn(careProviderId)
                .when(hsaOrganizationsService)
                .getVardgivareOfVardenhet(arende.getEnhetId());
        }

        @Test
        void shallIncludeAnswerQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.ANSWER_QUESTION);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertInclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallExcludeAnswerQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.ANSWER_QUESTION);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertExclude(actualLinks, ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallExcludeHandleQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertExclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
        }


        @Test
        void shallIncludeComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertInclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertExclude(actualLinks, ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallIncludeCannotComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertInclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeCannotComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(question);

            assertExclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }
    }

    @Nested
    class ListQuestion {

        @BeforeEach
        void setUp() {
            question = Question.builder()
                .id("questionId")
                .sent(LocalDateTime.now())
                .build();
            resourceLinkDTO = new ResourceLinkDTO();

            doReturn(Collections.singletonList(resourceLinkDTO))
                .when(getQuestionsAvailableFunctionsService)
                .get(question);

            final var arende = new Arende();
            arende.setEnhetId("unitId");
            arende.setPatientPersonId("191212121212");
            doReturn(arende)
                .when(arendeService)
                .getArende(question.getId());

            final var careProviderId = "careProviderId";
            doReturn(careProviderId)
                .when(hsaOrganizationsService)
                .getVardgivareOfVardenhet(arende.getEnhetId());
        }

        @Test
        void shallIncludeAnswerQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.ANSWER_QUESTION);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertInclude(actualLinks.get(question), ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallExcludeAnswerQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.ANSWER_QUESTION);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertExclude(actualLinks.get(question), ResourceLinkTypeDTO.ANSWER_QUESTION);
        }

        @Test
        void shallIncludeHandleQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertInclude(actualLinks.get(question), ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallExcludeHandleQuestion() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertExclude(actualLinks.get(question), ResourceLinkTypeDTO.HANDLE_QUESTION);
        }

        @Test
        void shallIncludeComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertInclude(actualLinks.get(question), ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertExclude(actualLinks.get(question), ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallIncludeCannotComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertInclude(actualLinks.get(question), ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }

        @Test
        void shallExcludeCannotComplementCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper)
                .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));

            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

            assertExclude(actualLinks.get(question), ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
        }
    }

    private void assertInclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNotNull(actualResourceLink, () -> String.format("Expected resource link with type '%s'", type));
    }

    private void assertExclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNull(actualResourceLink, () -> String.format("Don't expect resource link with type '%s'", type));
    }

    private ResourceLinkDTO get(List<ResourceLinkDTO> resourceLinks, ResourceLinkTypeDTO type) {
        return resourceLinks.stream()
            .filter(resourceLinkDTO -> resourceLinkDTO.getType().equals(type))
            .findFirst()
            .orElse(null);
    }
}