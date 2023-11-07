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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

class CertificateInfoSmittbararpenningFunctionTest {

    private CertificateInfoSmittbararpenningFunction certificateInfoSmittbararpenningFunction;
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String NOT_AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_28";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_TITLE = "Avstängning enligt smittskyddslagen";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_NAME = "Presentera informationsruta";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_BODY = "I intyg som gäller avstängning enligt smittskyddslagen kan"
        + " du inte dölja din diagnos. När du klickar på 'Skriv ut intyg' hämtas hela intyget.";
    private static final AvailableFunctionDTO EXPECTED_SMITTSKYDD_FUNCTION = AvailableFunctionDTO.create(
        AvailableFunctionTypeDTO.INFO,
        AVSTANGNING_SMITTSKYDD_INFO_TITLE,
        AVSTANGNING_SMITTSKYDD_INFO_NAME,
        AVSTANGNING_SMITTSKYDD_INFO_BODY
    );

    @BeforeEach
    void setUp() {
        certificateInfoSmittbararpenningFunction = new CertificateInfoSmittbararpenningFunction();
    }

    @Test
    void shouldReturnEmptyListIfTypeIsNotSupported() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .build()
        );
        final var availableFunction = certificateInfoSmittbararpenningFunction.get(CERTIFICATE);
        assertTrue(availableFunction.isEmpty());
    }

    @Test
    void shouldReturnEmptyListIfQuestionIdIsNotMatching() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .build()
        );
        CERTIFICATE.setData(
            Map.of(NOT_AVSTANGNING_SMITTSKYDD_QUESTION_ID,
                CertificateDataElement.builder()
                    .value(
                        CertificateDataValueBoolean.builder()
                            .selected(true)
                            .build()
                    )
                    .build())
        );
        final var availableFunction = certificateInfoSmittbararpenningFunction.get(CERTIFICATE);
        assertTrue(availableFunction.isEmpty());
    }

    @Test
    void shouldReturnAvailableFunctionInfoIfCertificateIsAG7804WithSmittbararpenningTrue() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .type(Ag7804EntryPoint.MODULE_ID)
                .build()
        );
        CERTIFICATE.setData(
            Map.of(AVSTANGNING_SMITTSKYDD_QUESTION_ID,
                CertificateDataElement.builder()
                    .value(
                        CertificateDataValueBoolean.builder()
                            .selected(true)
                            .build()
                    )
                    .build())
        );
        final var availableFunction = certificateInfoSmittbararpenningFunction.get(CERTIFICATE);
        assertTrue(availableFunction.contains(EXPECTED_SMITTSKYDD_FUNCTION));
    }

    @Test
    void shouldNotReturnAvailableFunctionInfoIfCertificateIsAG7804WithSmittbararpenningFalse() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .type(Ag7804EntryPoint.MODULE_ID)
                .build()
        );
        CERTIFICATE.setData(
            Map.of(AVSTANGNING_SMITTSKYDD_QUESTION_ID,
                CertificateDataElement.builder()
                    .value(
                        CertificateDataValueBoolean.builder()
                            .selected(false)
                            .build()
                    )
                    .build())
        );
        final var availableFunction = certificateInfoSmittbararpenningFunction.get(CERTIFICATE);
        assertFalse(availableFunction.contains(EXPECTED_SMITTSKYDD_FUNCTION));
    }

    @Test
    void shouldNotReturnAvailableFunctionInfoIfCertificateIsAG7804WithSmittbararpenningNull() {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .type(Ag7804EntryPoint.MODULE_ID)
                .build()
        );
        CERTIFICATE.setData(
            Map.of(AVSTANGNING_SMITTSKYDD_QUESTION_ID,
                CertificateDataElement.builder()
                    .value(
                        CertificateDataValueBoolean.builder()
                            .build()
                    )
                    .build())
        );
        final var availableFunction = certificateInfoSmittbararpenningFunction.get(CERTIFICATE);
        assertFalse(availableFunction.contains(EXPECTED_SMITTSKYDD_FUNCTION));
    }
}
