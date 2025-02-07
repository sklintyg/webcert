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

package se.inera.intyg.webcert.web.service.facade.question.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class FragaSvarToQuestionConverterImplTest {

    private static final String CERTIFICATE_ID = "certificateId";

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    private FragaSvarToQuestionConverterImpl fragaSvarToQuestionConverter;
    private FragaSvar fragaSvar;

    @BeforeEach
    void setUp() {
        fragaSvar = new FragaSvar();
        fragaSvar.setInternReferens(1L);
        final var vardperson = new Vardperson();
        vardperson.setNamn("namn");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setKompletteringar(Collections.emptySet());
        final var intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(CERTIFICATE_ID);
        fragaSvar.setIntygsReferens(intygsReferens);
        fragaSvarToQuestionConverter = new FragaSvarToQuestionConverterImpl(getCertificateFacadeService);
    }

    @Test
    void shallReturnQuestion() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertNotNull(actualQuestions);
    }

    @Test
    void shallHandleNullValue() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(null);
        assertNull(actualQuestions);
    }

    @Test
    void shallIncludeEmptyComplements() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertEquals(0, actualQuestions.getComplements().length);
    }

    @Test
    void shallIncludeId() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertEquals(fragaSvar.getInternReferens().toString(), actualQuestions.getId());
    }

    @Test
    void shallIncludeCertificateId() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertEquals(fragaSvar.getIntygsReferens().getIntygsId(), actualQuestions.getCertificateId());
    }

    @Test
    void shallIncludeAuthor() {
        final var expectedResult = "Försäkringskassan";
        fragaSvar.setFrageStallare("FK");

        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

        assertEquals(expectedResult, actualQuestions.getAuthor());
    }

    @Test
    void shallIncludeSent() {
        final var expectedResult = LocalDateTime.now();
        fragaSvar.setFrageSkickadDatum(expectedResult);
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

        assertEquals(expectedResult, actualQuestions.getSent());
    }

    @Test
    void shallIncludeMessage() {
        final var expectedResult = "message";
        fragaSvar.setFrageText(expectedResult);
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

        assertEquals(expectedResult, actualQuestions.getMessage());
    }

    @Test
    void shallIncludeMessageWithKompletteringar() {
        fragaSvar.setFrageText("frageTex");
        final var komplettering = new Komplettering();
        komplettering.setFalt("falt");
        komplettering.setText("text");
        fragaSvar.setKompletteringar(Set.of(komplettering));
        StringBuilder stringBuilder = new StringBuilder();
        final var expectedResult = stringBuilder.append(fragaSvar.getFrageText()).append("\n\n").append("falt").append("\n\n")
            .append("text")
            .toString();
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

        assertEquals(expectedResult, actualQuestions.getMessage());
    }

    @Nested
    class IncludeSubjectTests {

        @Test
        void shallIncludeSubjectKomplettering() {
            final var expectedResult = "Komplettering";
            fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }

        @Test
        void shallIncludeSubjectOvrigt() {
            final var expectedResult = "Övrigt";
            fragaSvar.setAmne(Amne.OVRIGT);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }

        @Test
        void shallIncludeSubjectKontakt() {
            final var expectedResult = "Kontakt";
            fragaSvar.setAmne(Amne.KONTAKT);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }

        @Test
        void shallIncludeSubjectAvstamningsmote() {
            final var expectedResult = "Avstämningsmöte";
            fragaSvar.setAmne(Amne.AVSTAMNINGSMOTE);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }

        @Test
        void shallIncludeSubjectPaminnelse() {
            final var expectedResult = "Påminnelse";
            fragaSvar.setAmne(Amne.PAMINNELSE);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }

        @Test
        void shallIncludeSubjectArbetstidsforlaggning() {
            final var expectedResult = "Arbetstidsförläggning";
            fragaSvar.setAmne(Amne.ARBETSTIDSFORLAGGNING);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getSubject());
        }
    }

    @Test
    void shallIncludeLastUpdate() {
        final var expectedResult = LocalDateTime.now();
        fragaSvar.setSvarSkickadDatum(expectedResult);
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

        assertEquals(expectedResult, actualQuestions.getLastUpdate());
    }

    @Nested
    class IncludeTypeTests {

        @Test
        void shallIncludeTypeKomplettering() {
            final var expectedResult = QuestionType.COMPLEMENT;
            fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }

        @Test
        void shallIncludeTypeMakulering() {
            final var expectedResult = QuestionType.COORDINATION;
            fragaSvar.setAmne(Amne.MAKULERING_AV_LAKARINTYG);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }

        @Test
        void shallIncludeTypeAvstamningsmote() {
            final var expectedResult = QuestionType.COORDINATION;
            fragaSvar.setAmne(Amne.AVSTAMNINGSMOTE);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }


        @Test
        void shallIncludeTypeKontakt() {
            final var expectedResult = QuestionType.CONTACT;
            fragaSvar.setAmne(Amne.KONTAKT);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }

        @Test
        void shallIncludeTypeArbetstidsforlaggning() {
            final var expectedResult = QuestionType.COORDINATION;
            fragaSvar.setAmne(Amne.ARBETSTIDSFORLAGGNING);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }

        @Test
        void shallIncludeTypePaminnelse() {
            final var expectedResult = QuestionType.COORDINATION;
            fragaSvar.setAmne(Amne.PAMINNELSE);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }

        @Test
        void shallIncludeTypeOvrigt() {
            final var expectedResult = QuestionType.OTHER;
            fragaSvar.setAmne(Amne.OVRIGT);
            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);

            assertEquals(expectedResult, actualQuestions.getType());
        }
    }

    @Test
    void shallIncludeReminders() {
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertNotNull(actualQuestions.getReminders());
    }

    @Test
    void shallIncludeIsHandled() {
        fragaSvar.setStatus(Status.CLOSED);
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertTrue(actualQuestions.isHandled());
    }

    @Test
    void shallIncludeIsForwarded() {
        fragaSvar.setVidarebefordrad(true);
        final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
        assertTrue(actualQuestions.isForwarded());
    }

    @Nested
    class IncludeAnsweredByCertificateTests {

        @Test
        void shallIncludeAnsweredByCertificate() {
            fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            fragaSvar.setKompletteringar(kompletteringTestSetup());

            Certificate certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .relations(
                        CertificateRelations.builder()
                            .children(
                                new CertificateRelation[]{
                                    CertificateRelation.builder()
                                        .type(CertificateRelationType.COMPLEMENTED)
                                        .created(LocalDateTime.now().plusDays(1))
                                        .status(CertificateStatus.SIGNED)
                                        .build()
                                }
                            ).build()
                    )
                    .build()
            );

            fragaSvar.setSvarSkickadDatum(LocalDateTime.now());

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true);

            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
            assertNotNull(actualQuestions.getAnsweredByCertificate());
        }

        @Test
        void shallNotIncludeAnsweredByCertificateIfChildrenHasStatusRevoked() {
            fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            fragaSvar.setKompletteringar(kompletteringTestSetup());

            Certificate certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .relations(
                        CertificateRelations.builder()
                            .children(
                                new CertificateRelation[]{
                                    CertificateRelation.builder()
                                        .type(CertificateRelationType.COMPLEMENTED)
                                        .created(LocalDateTime.now().plusDays(1))
                                        .status(CertificateStatus.REVOKED)
                                        .build()
                                }
                            ).build()
                    )
                    .build()
            );

            fragaSvar.setSvarSkickadDatum(LocalDateTime.now());

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true);

            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
            assertNull(actualQuestions.getAnsweredByCertificate());
        }

        @Test
        void shallNotIncludeAnsweredByCertificateIfChildrenWasCreatedBeforeSvarWasSent() {
            fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            fragaSvar.setKompletteringar(kompletteringTestSetup());

            Certificate certificate = new Certificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .relations(
                        CertificateRelations.builder()
                            .children(
                                new CertificateRelation[]{
                                    CertificateRelation.builder()
                                        .type(CertificateRelationType.COMPLEMENTED)
                                        .created(LocalDateTime.now().plusDays(1))
                                        .status(CertificateStatus.REVOKED)
                                        .build()
                                }
                            ).build()
                    )
                    .build()
            );

            fragaSvar.setSvarSkickadDatum(LocalDateTime.now().plusDays(2));

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(CERTIFICATE_ID, false, true);

            final var actualQuestions = fragaSvarToQuestionConverter.convert(fragaSvar);
            assertNull(actualQuestions.getAnsweredByCertificate());
        }
    }

    private Set<Komplettering> kompletteringTestSetup() {
        final var komplettering = new Komplettering();
        komplettering.setFalt("falt1");
        komplettering.setText("text");
        final var komplettering2 = new Komplettering();
        komplettering2.setFalt("falt2");
        komplettering2.setText("text");
        final var komplettering3 = new Komplettering();
        komplettering2.setFalt("falt3");
        komplettering2.setText("text");
        return Set.of(komplettering, komplettering2, komplettering3);
    }
}
