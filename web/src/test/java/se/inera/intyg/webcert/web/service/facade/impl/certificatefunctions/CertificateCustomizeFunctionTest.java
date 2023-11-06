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
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateCustomizeFunctionTest {

    private static final String RESOURCE_LINK_DESCRIPTION = "Information om diagnos kan vara viktig för din arbetsgivare."
        + " Det kan underlätta anpassning av din arbetssituation. Det kan också göra att du snabbare kommer tillbaka till arbetet.";
    private static final String RESOURCE_LINK_BODY = "När du skriver ut ett läkarintyg du ska lämna till din arbetsgivare kan du "
        + "välja om du vill att din diagnos ska visas eller döljas. Ingen annan information kan döljas. ";
    private static final String RESOURCE_LINK_TITLE = "Vill du visa eller dölja diagnos?";
    private static final String RESOURCE_LINK_NAME = "Anpassa intyget";

    private static final String CORRECT_TYPE = Ag7804EntryPoint.MODULE_ID;
    private static final String WRONG_TYPE = Fk7263EntryPoint.MODULE_ID;
    private static final String QUESTION_SMITTBARAR_PENNING = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String NOT_QUESTION_SMITTBARAR_PENNING = "NOT_AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final ResourceLinkDTO EXPECTED_RESOURCE_LINK = ResourceLinkDTO.create(
        ResourceLinkTypeDTO.CUSTOMIZE_CERTIFICATE,
        RESOURCE_LINK_TITLE,
        RESOURCE_LINK_NAME,
        RESOURCE_LINK_DESCRIPTION,
        RESOURCE_LINK_BODY
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
        assertEquals(EXPECTED_RESOURCE_LINK, result.get());
    }

    @Test
    void shouldReturnResourceLinkCustomizeCertificateIfCertificateIsOfTypeAG7804AndQuestionSmittskyddNull() {
        final var certificate = buildCertificate(new Certificate(), CORRECT_TYPE, QUESTION_SMITTBARAR_PENNING, null);
        final var result = certificateCustomizeFunction.get(certificate);
        assertEquals(EXPECTED_RESOURCE_LINK, result.get());
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
