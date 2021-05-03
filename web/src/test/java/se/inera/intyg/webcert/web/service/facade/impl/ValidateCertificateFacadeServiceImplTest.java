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

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.builder.CertificateMetadataBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;

@ExtendWith(MockitoExtension.class)
class ValidateCertificateFacadeServiceImplTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private ValidateCertificateFacadeServiceImpl validateCertificateFacadeService;

    private Certificate certificate;
    private DraftValidation draftValidation = new DraftValidation();

    @BeforeEach
    void setup() throws Exception {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadataBuilder.create()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .build()
            )
            .build();

        final var currentCertificate = new Utkast();
        currentCertificate.setModel("currentCertificateJson");

        doReturn(currentCertificate)
            .when(utkastService)
            .getDraft(CERTIFICATE_ID);

        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(intygModuleRegistry)
            .getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);

        final var certificateJson = "json";
        doReturn(certificateJson)
            .when(moduleApi)
            .getJsonFromCertificate(certificate, currentCertificate.getModel());

        doReturn(draftValidation)
            .when(utkastService)
            .validateDraft(CERTIFICATE_ID, CERTIFICATE_TYPE, certificateJson);
    }

    @Test
    void shallReturnEmptyValidationErrorsWhenValid() {
        draftValidation.setStatus(ValidationStatus.VALID);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(0, actualValidationErrors.length);
    }

    @Test
    void shallIncludeCategoryInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        final var expectedCategory = "expectedCategory";
        draftValidationMessage.setCategory(expectedCategory);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(expectedCategory, actualValidationErrors[0].getCategory());
    }

    @Test
    void shallIncludeFieldInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        final var expectedField = "expectedField";
        draftValidationMessage.setField(expectedField);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(expectedField, actualValidationErrors[0].getField());
    }

    @Test
    void shallIncludeQuestionIdInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        final var expectedId = "expectedId";
        draftValidationMessage.setQuestionId(expectedId);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(expectedId, actualValidationErrors[0].getId());
    }

    @Test
    void shallIncludeTypeInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        final var expectedType = "EMPTY";
        draftValidationMessage.setType(ValidationMessageType.EMPTY);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(expectedType, actualValidationErrors[0].getType());
    }

    private DraftValidationMessage addValidationMessage() {
        draftValidation.setStatus(ValidationStatus.INVALID);
        final var validationMessage = new DraftValidationMessage(
            "expectedCategory",
            "expectedField",
            ValidationMessageType.BLANK,
            "expectedMessage",
            "expectedDynamicKey",
            "expectedQuestionId"
        );
        draftValidation.addMessage(validationMessage);
        return validationMessage;
    }
}