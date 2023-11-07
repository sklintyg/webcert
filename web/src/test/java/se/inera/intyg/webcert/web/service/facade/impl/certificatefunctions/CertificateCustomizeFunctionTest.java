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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.common.ag7804.converter.RespConstants.DIAGNOS_SVAR_JSON_ID_6;

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
import se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.CertificateCustomizeFunction;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationTypeDto;

@ExtendWith(MockitoExtension.class)
class CertificateCustomizeFunctionTest {

    private static final String AVAILABLE_FUNCTION_BODY = "När du skriver ut ett läkarintyg du ska lämna till din arbetsgivare kan du "
        + "välja om du vill att din diagnos ska visas eller döljas. Ingen annan information kan döljas. ";
    private static final String AVAILABLE_FUNCTION_TITLE = "Vill du visa eller dölja diagnos?";
    private static final String AVAILABLE_FUNCTION_NAME = "Anpassa intyget för utskrift";
    private static final String INFORMATION_ALERT_TEXT = "Information om diagnos kan vara viktig för din arbetsgivare."
        + " Det kan underlätta anpassning av din arbetssituation. Det kan också göra att du snabbare kommer tillbaka till arbetet.";
    private static final String OPTIONAL_FIELD_DIAGNOSER_SHOW_ID = DIAGNOS_SVAR_JSON_ID_6;
    private static final String OPTIONAL_FIELD_DIAGNOSER_HIDE_ID = "!" + DIAGNOS_SVAR_JSON_ID_6;
    private static final String SHOW_DIAGNOSIS = "Visa Diagnos";
    private static final String HIDE_DIAGNOSIS = "Dölj Diagnos";
    private static final String HIDE_DIAGNOSIS_ALERT_ID = "hideDiagnosisAlert";
    private static final String CORRECT_TYPE = Ag7804EntryPoint.MODULE_ID;
    private static final String WRONG_TYPE = Fk7263EntryPoint.MODULE_ID;
    private static final String QUESTION_SMITTBARAR_PENNING = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String NOT_QUESTION_SMITTBARAR_PENNING = "NOT_AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final List<InformationDTO> EXPECTED_INFORMATION = List.of(
        InformationDTO.create(
            OPTIONAL_FIELD_DIAGNOSER_SHOW_ID,
            SHOW_DIAGNOSIS,
            InformationTypeDto.RADIO_BUTTON
        ),
        InformationDTO.create(
            OPTIONAL_FIELD_DIAGNOSER_HIDE_ID,
            HIDE_DIAGNOSIS,
            InformationTypeDto.RADIO_BUTTON
        ),
        InformationDTO.create(
            HIDE_DIAGNOSIS_ALERT_ID,
            INFORMATION_ALERT_TEXT,
            InformationTypeDto.ALERT,
            OPTIONAL_FIELD_DIAGNOSER_HIDE_ID
        )
    );
    private static final AvailableFunctionDTO EXPECTED_AVAILABLE_FUNCTION = AvailableFunctionDTO.create(
        AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE,
        AVAILABLE_FUNCTION_TITLE,
        AVAILABLE_FUNCTION_NAME,
        AVAILABLE_FUNCTION_BODY,
        EXPECTED_INFORMATION
    );

    @InjectMocks
    private CertificateCustomizeFunction certificateCustomizeFunction;

    @Test
    void shouldReturnEmptyListIfCertificateTypeIsWrongType() {
        final var certificate = buildCertificate(new Certificate(), WRONG_TYPE, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificateCustomizeFunction.get(certificate);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListIfCertificateIsOfTypeAG7804AndSmittbararpenningIsTrue() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, true);
        final var result = certificateCustomizeFunction.get(certificate);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListIfCertificateIsOfTypeAG7804AndSmittbararpenningQuestionIsMissing() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, NOT_QUESTION_SMITTBARAR_PENNING, true);
        final var result = certificateCustomizeFunction.get(certificate);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnResourceLinkCustomizeCertificateIfCertificateIsOfTypeAG7804AndQuestionSmittskyddFalse() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, false);
        final var result = certificateCustomizeFunction.get(certificate);
        assertEquals(EXPECTED_AVAILABLE_FUNCTION, result.get());
    }

    @Test
    void shouldReturnResourceLinkCustomizeCertificateIfCertificateIsOfTypeAG7804AndQuestionSmittskyddNull() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificateCustomizeFunction.get(certificate);
        assertEquals(EXPECTED_AVAILABLE_FUNCTION, result.get());
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
