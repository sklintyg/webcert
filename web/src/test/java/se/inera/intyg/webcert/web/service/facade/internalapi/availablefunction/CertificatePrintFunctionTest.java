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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@ExtendWith(MockitoExtension.class)
class CertificatePrintFunctionTest {

    private static final String AG7804 = Ag7804EntryPoint.MODULE_ID;
    private static final String AG114 = Ag114EntryPoint.MODULE_ID;
    private static final String FK7263 = Fk7263EntryPoint.MODULE_ID;
    private static final String QUESTION_SMITTBARAR_PENNING = "27";
    private static final String NOT_QUESTION_SMITTBARAR_PENNING = "NOT_AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    public static final String FILE_NAME = "lakarintyg_for_sjukpenning";
    public static final String NAME = "Läkarintyg för sjukpenning";
    private static final AvailableFunctionDTO EXPECTED_CUSTOMIZE_FUNCTION = AvailableFunctionFactory.customizePrint(true, FILE_NAME);
    private static final AvailableFunctionDTO EXPECTED_PRINT_FUNCTION = AvailableFunctionFactory.print(true, FILE_NAME);
    private static final AvailableFunctionDTO EXPECTED_SMITTBARAR_PENNING_FUNCTION = AvailableFunctionFactory.avstangningSmittskydd(true);

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    private CertificatePrintFunction certificatePrintFunction;

    @Nested
    class ActiveFeature {

        @BeforeEach
        void setup() {
            when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
                .thenReturn(true);
        }

        @Test
        void shouldOnlyReturnPrintCertificateIfTypeIsNotAg7804OrAg114() {
            final var certificate = buildCertificate(new Certificate(), FK7263, QUESTION_SMITTBARAR_PENNING, null);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_PRINT_FUNCTION), result);
        }

        @Test
        void shoulReturnPrintCertificateAndSmittbarareInfoIfTypeIsAg7804ButQuestionSmittbararpenningIsTrue() {
            final var certificate = buildCertificate(new Certificate(), AG7804, QUESTION_SMITTBARAR_PENNING, true);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_PRINT_FUNCTION, EXPECTED_SMITTBARAR_PENNING_FUNCTION), result);
        }

        @Test
        void shouldOnlyReturnPrintCertificateIfTypeIsAg7804ButQuestionSmittbararpenningIsMissing() {
            final var certificate = buildCertificate(new Certificate(), AG7804, NOT_QUESTION_SMITTBARAR_PENNING, false);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_PRINT_FUNCTION), result);
        }

        @Test
        void shouldReturnCustomizePrintCertificateIfTypeIsAg7804AndQuestionSmittbararpenningIsFalse() {
            final var certificate = buildCertificate(new Certificate(), AG7804, QUESTION_SMITTBARAR_PENNING, false);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_CUSTOMIZE_FUNCTION), result);
        }

        @Test
        void shouldReturnCustomizePrintCertificateIfTypeIsAg7804AndQuestionSmittbararpenningIsNull() {
            final var certificate = buildCertificate(new Certificate(), AG7804, QUESTION_SMITTBARAR_PENNING, null);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_CUSTOMIZE_FUNCTION), result);
        }

        @Test
        void shouldReturnCustomizePrintCertificateIfTypeIsAg114() {
            final var certificate = buildCertificate(new Certificate(), AG114, NOT_QUESTION_SMITTBARAR_PENNING, false);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(List.of(EXPECTED_CUSTOMIZE_FUNCTION), result);
        }

        @Test
        void shouldReturnEmptyCertificateIsReplaced() {
            final var certificate = buildCertificateWithRelation(new Certificate(), CertificateRelationType.REPLACED);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(Collections.emptyList(), result);
        }

        @Test
        void shouldReturnEmptyCertificateIsComplemented() {
            final var certificate = buildCertificateWithRelation(new Certificate(), CertificateRelationType.COMPLEMENTED);
            final var result = certificatePrintFunction.get(certificate);
            assertEquals(Collections.emptyList(), result);
        }
    }

    @Test
    void shouldReturnEmptyIfFeatureIsInactive() {
        final var certificate = buildCertificate(new Certificate(), AG7804, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(Collections.emptyList(), result);
    }

    private static Certificate buildCertificateWithRelation(Certificate certificate, CertificateRelationType type) {
        CertificateRelation[] relationChildren = {
            CertificateRelation.builder()
                .type(type)
                .status(CertificateStatus.SIGNED)
                .build()
        };
        final var relations = CertificateRelations.builder()
            .children(relationChildren)
            .build();
        return buildCertificate(certificate, AG7804, QUESTION_SMITTBARAR_PENNING, true, relations);
    }

    private static Certificate buildCertificate(Certificate certificate, String type, String questionId,
        Boolean selected) {
        return buildCertificate(certificate, type, questionId, selected, null);
    }


    private static Certificate buildCertificate(Certificate certificate, String type, String questionId,
        Boolean selected, CertificateRelations certificateRelations) {
        certificate.setMetadata(
            CertificateMetadata.builder()
                .type(type)
                .name(NAME)
                .relations(certificateRelations)
                .build()
        );
        certificate.setData(
            Map.of(questionId, CertificateDataElement.builder()
                .value(
                    CertificateDataValueBoolean.builder()
                        .selected(selected)
                        .build()
                )
                .build())
        );
        return certificate;
    }
}
