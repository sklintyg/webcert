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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.messages.CertificateMessagesProvider;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigCheckboxMultipleCode;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.validator.CertificateValidator;
import se.inera.intyg.webcert.web.service.facade.validator.CertificateValidatorProvider;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;

@ExtendWith(MockitoExtension.class)
class ValidateCertificateFacadeServiceImplTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "lisjp";
    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    private static final String EXPECTED_TEXT = "expectedText";
    private static final String EXPECTED_TEXT2 = "expectedText2";
    private static final String EXPECTED_MESSAGE = "expectedMessage";
    private static final String QUESTION_ID_40 = "40";

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private CertificateValidatorProvider certificateValidatorProvider;

    @InjectMocks
    private ValidateCertificateFacadeServiceImpl validateCertificateFacadeService;

    private Certificate certificate;
    private final DraftValidation draftValidation = new DraftValidation();

    @BeforeEach
    void setup() throws Exception {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .type(CERTIFICATE_TYPE)
                    .typeVersion(CERTIFICATE_TYPE_VERSION)
                    .build()
            ).addElement(CertificateDataElement.builder()
                .id(QUESTION_ID_40)
                .config(CertificateDataConfigCheckboxMultipleCode.builder().build())
                .build())
            .build();

        final var currentCertificate = new Utkast();
        currentCertificate.setModel("currentCertificateJson");

        doReturn(currentCertificate)
            .when(utkastService)
            .getDraft(CERTIFICATE_ID, false);

        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(intygModuleRegistry)
            .getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);

        final var certificateJson = "json";
        doReturn(certificateJson)
            .when(moduleApi)
            .getJsonFromCertificate(certificate, currentCertificate.getModel());

        final var messagesProvider = mock(CertificateMessagesProvider.class);
        lenient().doReturn(messagesProvider)
            .when(moduleApi)
            .getMessagesProvider();

        lenient().doReturn(EXPECTED_TEXT)
            .when(messagesProvider)
            .get(EXPECTED_MESSAGE);

        lenient().doReturn(EXPECTED_TEXT2)
            .when(messagesProvider)
            .get("common.validation.ue-checkgroup-disabled.empty");

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

        final var expectedField = "expectedQuestionId";
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

    @Test
    void shallIncludeTextInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        draftValidationMessage.setMessage(EXPECTED_MESSAGE);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(EXPECTED_TEXT, actualValidationErrors[0].getText());
    }

    @Test
    void shallReturnValidationErrorEvenIfQuestionIdIsMissingAndMessageIsEmpty() {
        final var draftValidationMessage = addValidationMessage();

        draftValidationMessage.setQuestionId(null);
        draftValidationMessage.setMessage(null);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(1, actualValidationErrors.length);
    }

    @Test
    void shallIncludeTextDependingOnComponentInValidationError() {
        final var draftValidationMessage = addValidationMessage();

        draftValidationMessage.setType(ValidationMessageType.EMPTY);
        draftValidationMessage.setMessage(null);
        draftValidationMessage.setQuestionId(QUESTION_ID_40);

        final var actualValidationErrors = validateCertificateFacadeService.validate(certificate);

        assertEquals(EXPECTED_TEXT2, actualValidationErrors[0].getText());
    }

    private DraftValidationMessage addValidationMessage() {
        draftValidation.setStatus(ValidationStatus.INVALID);
        final var validationMessage = new DraftValidationMessage(
            "expectedCategory",
            "expectedField",
            ValidationMessageType.EMPTY,
            "expectedMessage",
            null,
            "expectedQuestionId"
        );
        draftValidation.addMessage(validationMessage);
        return validationMessage;
    }

    @Nested
    class CertificateSpecificValidation {

        @Test
        void shouldNotCallValidatorWhenValidatorProviderReturnsEmpty() {
            doReturn(Optional.empty()).when(certificateValidatorProvider).get(CERTIFICATE_TYPE);

            validateCertificateFacadeService.validate(certificate);

            verify(certificateValidatorProvider).get(CERTIFICATE_TYPE);
        }

        @Test
        void shouldCallCertificateValidatorWhenProviderReturnsValidator() {
            final var mockValidator = mock(CertificateValidator.class);
            doReturn(Optional.of(mockValidator)).when(certificateValidatorProvider).get(CERTIFICATE_TYPE);

            validateCertificateFacadeService.validate(certificate);

            verify(mockValidator).validate(eq(certificate), any(DraftValidation.class));
        }
    }
}
