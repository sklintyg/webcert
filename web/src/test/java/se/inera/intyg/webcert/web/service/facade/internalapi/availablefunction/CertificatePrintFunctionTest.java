/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@ExtendWith(MockitoExtension.class)
class CertificatePrintFunctionTest {

    private static final String CORRECT_TYPE = Ag7804EntryPoint.MODULE_ID;
    private static final String WRONG_TYPE = Fk7263EntryPoint.MODULE_ID;
    private static final String QUESTION_SMITTBARAR_PENNING = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String NOT_QUESTION_SMITTBARAR_PENNING = "NOT_AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final AvailableFunctionDTO EXPECTED_CUSTOMIZE_FUNCTION = AvailableFunctionFactory.customizePrint();
    private static final AvailableFunctionDTO EXPECTED_PRINT_FUNCTION = AvailableFunctionFactory.print();
    private static final AvailableFunctionDTO EXPECTED_SMITTBARAR_PENNING_FUNCTION = AvailableFunctionFactory.avstangningSmittskydd();

    @InjectMocks
    private CertificatePrintFunction certificatePrintFunction;

    @Test
    void shouldOnlyReturnPrintCertificateIfTypeIsNotAg7804() {
        final var certificate = buildCertificate(new Certificate(), WRONG_TYPE, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(List.of(EXPECTED_PRINT_FUNCTION), result);
    }

    @Test
    void shoulReturnPrintCertificateAndSmittbarareInfoIfTypeIsAg7804ButQuestionSmittbararpenningIsTrue() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, true);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(List.of(EXPECTED_SMITTBARAR_PENNING_FUNCTION, EXPECTED_PRINT_FUNCTION), result);
    }

    @Test
    void shouldOnlyReturnPrintCertificateIfTypeIsAg7804ButQuestionSmittbararpenningIsMissing() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, NOT_QUESTION_SMITTBARAR_PENNING, false);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(List.of(EXPECTED_PRINT_FUNCTION), result);
    }

    @Test
    void shouldReturnCustomizePrintCertificateIfTypeIsAg7804AndQuestionSmittbararpenningIsFalse() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, false);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(List.of(EXPECTED_CUSTOMIZE_FUNCTION), result);
    }

    @Test
    void shouldReturnCustomizePrintCertificateIfTypeIsAg7804AndQuestionSmittbararpenningIsNull() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificatePrintFunction.get(certificate);
        assertEquals(List.of(EXPECTED_CUSTOMIZE_FUNCTION), result);
    }


    private static Certificate buildCertificate(Certificate certificate, String type, String questionId,
        Boolean selected) {
        certificate.setMetadata(
            CertificateMetadata.builder()
                .type(type)
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
