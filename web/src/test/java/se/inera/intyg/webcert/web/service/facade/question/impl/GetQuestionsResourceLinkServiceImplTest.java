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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetQuestionsResourceLinkServiceImplTest {

    private static final String TYPE = "TYPE";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String UNIT_ID = "UNIT_ID";

    @Mock
    private GetQuestionsAvailableFunctionsService getQuestionsAvailableFunctionsService;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private GetQuestionsResourceLinkServiceImpl getQuestionsResourceLinkService;

    private static final String CERTIFICATE_ID = "certificateId";
    private Question question;
    private Certificate certificate;
    private ResourceLinkDTO resourceLinkDTO;

    @Nested
    class UseResourceLinksFromQuestion {

        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String TITLE = "title";
        private static final String BODY = "body";

        @Test
        void shallUseResourceLinksProvidedInListOfQuestions() {
            final var question = Question.builder()
                .links(
                    List.of(
                        ResourceLink.builder()
                            .name(NAME)
                            .type(ResourceLinkTypeEnum.QUESTIONS)
                            .description(DESCRIPTION)
                            .body(BODY)
                            .enabled(true)
                            .title(TITLE)
                            .build()
                    )
                )
                .build();

            final var expectedResourceLink = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.valueOf(ResourceLinkTypeEnum.QUESTIONS.name()),
                TITLE,
                NAME,
                DESCRIPTION,
                BODY,
                true
            );

            final var questions = List.of(
                question
            );

            final var actualQuestionMap = getQuestionsResourceLinkService.get(questions);
            assertEquals(Map.of(question, List.of(expectedResourceLink)), actualQuestionMap);
        }

        @Test
        void shallUseResourceLinksProvidedInQuestion() {
            final var question = Question.builder()
                .links(
                    List.of(
                        ResourceLink.builder()
                            .name(NAME)
                            .type(ResourceLinkTypeEnum.QUESTIONS)
                            .description(DESCRIPTION)
                            .body(BODY)
                            .enabled(true)
                            .title(TITLE)
                            .build()
                    )
                )
                .build();

            final var expectedResourceLink = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.valueOf(ResourceLinkTypeEnum.QUESTIONS.name()),
                TITLE,
                NAME,
                DESCRIPTION,
                BODY,
                true
            );

            final var actualResourceLinks = getQuestionsResourceLinkService.get(question);
            assertEquals(List.of(expectedResourceLink), actualResourceLinks);
        }
    }

    @Nested
    class LinksNotProvidedFromCertificateService {

        @BeforeEach
        void setup() {
            certificate = new Certificate();
            final var metadata = CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(TYPE)
                .careProvider(
                    Unit.builder()
                        .unitId(CARE_PROVIDER_ID)
                        .build()
                )
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .build()
                )
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id("191212121212")
                                .build()
                        )
                        .build()
                )
                .build();

            certificate.setMetadata(metadata);

            when(getCertificateFacadeService.getCertificate(CERTIFICATE_ID, false, false))
                .thenReturn(certificate);
        }

        @Nested
        class AccessEvaluationParameter {

            @Nested
            class SingleQuestion {

                @BeforeEach
                void setup() {
                    question = Question.builder()
                        .certificateId(CERTIFICATE_ID)
                        .id("questionId")
                        .sent(LocalDateTime.now())
                        .build();

                    when(getQuestionsAvailableFunctionsService.get(question))
                        .thenReturn(
                            List.of(
                                ResourceLinkDTO.create(ResourceLinkTypeDTO.ANSWER_QUESTION, "", "", "", false))
                        );
                }

                @Test
                void shallCallGetCertificateOnlyOnce() {
                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(getCertificateFacadeService, times(1)).getCertificate(CERTIFICATE_ID, false, false);
                }

                @Test
                void shallSetType() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(certificateAccessServiceHelper, times(2)).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(TYPE, captor.getValue().getCertificateType());
                }

                @Test
                void shallSetUnit() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);
                    final var expectedUnit = new Vardenhet();
                    final var expectedCareProvider = new Vardgivare();
                    expectedCareProvider.setVardgivarid(CARE_PROVIDER_ID);
                    expectedUnit.setEnhetsid(UNIT_ID);
                    expectedUnit.setVardgivare(expectedCareProvider);

                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(certificateAccessServiceHelper, times(2)).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(expectedUnit, captor.getValue().getUnit());
                }

                @Test
                void shallSetTypeVersionNull() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(certificateAccessServiceHelper, times(2)).isAllowToAnswerAdminQuestion(captor.capture());

                    assertNull(captor.getValue().getCertificateTypeVersion());
                }

                @Test
                void shallSetTestCertificateFalse() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(certificateAccessServiceHelper, times(2)).isAllowToAnswerAdminQuestion(captor.capture());

                    assertFalse(captor.getValue().isTestCertificate());
                }

                @Test
                void shallSetPatientId() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(List.of(question, question));
                    verify(certificateAccessServiceHelper, times(2)).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(certificate.getMetadata().getPatient().getPersonId().getId(),
                        captor.getValue().getPatient().getOriginalPnr());
                }
            }

            @Nested
            class ListOfQuestions {

                @BeforeEach
                void setup() {
                    question = Question.builder()
                        .certificateId(CERTIFICATE_ID)
                        .id("questionId")
                        .sent(LocalDateTime.now())
                        .build();

                    when(getQuestionsAvailableFunctionsService.get(question))
                        .thenReturn(
                            List.of(
                                ResourceLinkDTO.create(ResourceLinkTypeDTO.ANSWER_QUESTION, "", "", "", false))
                        );
                }

                @Test
                void shallSetType() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(question);
                    verify(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(TYPE, captor.getValue().getCertificateType());
                }

                @Test
                void shallSetUnit() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);
                    final var expectedUnit = new Vardenhet();
                    final var expectedCareProvider = new Vardgivare();
                    expectedCareProvider.setVardgivarid(CARE_PROVIDER_ID);
                    expectedUnit.setEnhetsid(UNIT_ID);
                    expectedUnit.setVardgivare(expectedCareProvider);

                    getQuestionsResourceLinkService.get(question);
                    verify(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(expectedUnit, captor.getValue().getUnit());
                }

                @Test
                void shallSetTypeVersionNull() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(question);
                    verify(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(captor.capture());

                    assertNull(captor.getValue().getCertificateTypeVersion());
                }

                @Test
                void shallSetTestCertificateFalse() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(question);
                    verify(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(captor.capture());

                    assertFalse(captor.getValue().isTestCertificate());
                }

                @Test
                void shallSetPatientId() {
                    final var captor = ArgumentCaptor.forClass(AccessEvaluationParameters.class);

                    getQuestionsResourceLinkService.get(question);
                    verify(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(captor.capture());

                    assertEquals(certificate.getMetadata().getPatient().getPersonId().getId(),
                        captor.getValue().getPatient().getOriginalPnr());
                }
            }
        }

        @Test
        void shallIncludeNoLinksIfQuestionDraft() {
            question = Question.builder()
                .id("questionId")
                .certificateId(CERTIFICATE_ID)
                .build();
            final var actualLinks = getQuestionsResourceLinkService.get(question);
            assertEquals(0, actualLinks.size(), "Links should be empty");
        }

        @Test
        void shallIncludeNoLinksIfListHaveQuestionDraft() {
            question = Question.builder()
                .id("questionId")
                .certificateId(CERTIFICATE_ID)
                .build();
            final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));
            assertEquals(0, actualLinks.get(question).size(), "Links should be empty");
        }

        @Nested
        class SingleQuestion {

            @BeforeEach
            void setUp() {
                question = Question.builder()
                    .id("123")
                    .sent(LocalDateTime.now())
                    .certificateId(CERTIFICATE_ID)
                    .build();

                resourceLinkDTO = new ResourceLinkDTO();

                doReturn(Collections.singletonList(resourceLinkDTO))
                    .when(getQuestionsAvailableFunctionsService)
                    .get(question);
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

            @Test
            void shallIncludeHandleQuestionFragaSvar() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
                doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

                final var actualLinks = getQuestionsResourceLinkService.get(question);

                assertInclude(actualLinks, ResourceLinkTypeDTO.HANDLE_QUESTION);
            }

            @Test
            void shallExcludeHandleQuestionFragaSvar() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
                doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

                final var actualLinks = getQuestionsResourceLinkService.get(question);

                assertExclude(actualLinks, ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE);
            }

            @Test
            void shallIncludeForwardQuestion() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.FORWARD_QUESTION);
                doReturn(true).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));

                final var actualLinks = getQuestionsResourceLinkService.get(question);

                assertInclude(actualLinks, ResourceLinkTypeDTO.FORWARD_QUESTION);
            }

            @Test
            void shallExcludeForwardQuestion() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.FORWARD_QUESTION);
                doReturn(false).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));

                final var actualLinks = getQuestionsResourceLinkService.get(question);

                assertExclude(actualLinks, ResourceLinkTypeDTO.FORWARD_QUESTION);
            }
        }

        @Nested
        class ListQuestion {

            @BeforeEach
            void setUp() {
                question = Question.builder()
                    .id("123")
                    .sent(LocalDateTime.now())
                    .certificateId(CERTIFICATE_ID)
                    .build();
                resourceLinkDTO = new ResourceLinkDTO();

                doReturn(Collections.singletonList(resourceLinkDTO))
                    .when(getQuestionsAvailableFunctionsService)
                    .get(question);
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

            @Test
            void shallIncludeHandleQuestionFragaSvar() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
                doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

                final var actualLinks = getQuestionsResourceLinkService.get(Collections.singletonList(question));

                assertInclude(actualLinks.get(question), ResourceLinkTypeDTO.HANDLE_QUESTION);
            }

            @Test
            void shallExcludeHandleQuestionFragaSvar() {
                resourceLinkDTO.setType(ResourceLinkTypeDTO.HANDLE_QUESTION);
                doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));

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
}
