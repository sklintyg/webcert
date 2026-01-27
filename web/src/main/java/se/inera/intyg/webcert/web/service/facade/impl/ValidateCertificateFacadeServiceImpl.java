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

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;

@Service("validateCertificateFromWC")
public class ValidateCertificateFacadeServiceImpl implements ValidateCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;
    private final IntygModuleRegistry moduleRegistry;

    @Autowired
    public ValidateCertificateFacadeServiceImpl(UtkastService utkastService, IntygModuleRegistry moduleRegistry) {
        this.utkastService = utkastService;
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public ValidationErrorDTO[] validate(Certificate certificate) {

        final var moduleApi = getModuleApi(certificate);

        LOG.debug("Get certificate '{}' to validate", certificate.getMetadata().getId());
        final var currentCertificate = utkastService.getDraft(certificate.getMetadata().getId(), false);

        LOG.debug("Validate certificate '{}'", certificate.getMetadata().getId());
        final var draftValidation = utkastService.validateDraft(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            getJsonFromCertificate(moduleApi, certificate, currentCertificate.getModel(), certificate.getMetadata().getCreated())
        );

        LOG.debug("Convert validation result for certificate '{}'", certificate.getMetadata().getId());
        return convertDraftValidation(moduleApi, certificate, draftValidation);
    }

    private ModuleApi getModuleApi(Certificate certificate) {
        try {
            return moduleRegistry.getModuleApi(certificate.getMetadata().getType(), certificate.getMetadata().getTypeVersion());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String getJsonFromCertificate(ModuleApi moduleApi, Certificate certificate, String currentModel, LocalDateTime created) {
        try {
            return moduleApi.getJsonFromCertificate(certificate, currentModel, created);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private ValidationErrorDTO[] convertDraftValidation(ModuleApi moduleApi, Certificate certificate, DraftValidation draftValidation) {
        return draftValidation.getMessages().stream()
            .map(validationMessage -> convertValidationError(moduleApi, certificate, validationMessage))
            .toArray(ValidationErrorDTO[]::new);
    }

    private ValidationErrorDTO convertValidationError(ModuleApi moduleApi, Certificate certificate,
        DraftValidationMessage validationMessage) {
        final var validationError = new ValidationErrorDTO();
        validationError.setCategory(validationMessage.getCategory());
        validationError.setField(validationMessage.getField());
        validationError.setType(validationMessage.getType().name());
        validationError.setId(validationMessage.getQuestionId());
        validationError.setText(getValidationText(moduleApi, certificate, validationMessage.getMessage(), validationMessage.getType(),
            validationMessage.getQuestionId(), validationMessage.getDynamicKey()));
        return validationError;
    }

    private String getValidationText(ModuleApi moduleApi, Certificate certificate, String message,
        ValidationMessageType validationMessageType, String questionId, String dynamicKey) {
        final var key = resolveKey(certificate, message, validationMessageType, questionId);
        final var messageProvider = moduleApi.getMessagesProvider();
        if (dynamicKey == null) {
            return messageProvider.get(key);
        }
        return messageProvider.get(key, dynamicKey);
    }

    private String resolveKey(Certificate certificate, String message, ValidationMessageType validationMessageType, String questionId) {
        if (!Strings.isNullOrEmpty(message)) {
            return message;
        }

        final var certificateDataElement = certificate.getData().get(questionId);
        if (certificateDataElement == null) {
            return message;
        }
        final var componentType = certificateDataElement.getConfig().getType();
        final var oldComponentName = convertToOldName(componentType, certificate, questionId);
        return "common.validation." + oldComponentName + "." + validationMessageType.name().toLowerCase();
    }

    private String convertToOldName(CertificateDataConfigType componentType, Certificate certificate, String questionId) {
        switch (componentType) {
            case UE_CHECKBOX_MULTIPLE_CODE:
                if (isQuestion40InFK7804(certificate.getMetadata().getType(), questionId)) {
                    return "ue-checkgroup-disabled";
                }
                return "ue-checkgroup";
            case UE_CHECKBOX_BOOLEAN:
                return "ue-checkbox";
            case UE_CHECKBOX_MULTIPLE_DATE:
                return "ue-checkgroup";
            case UE_DIAGNOSES:
                return "ue-diagnos";
            case UE_TEXTAREA:
                return "ue-textarea";
            case UE_RADIO_BOOLEAN:
                return "ue-radio";
            case UE_RADIO_MULTIPLE_CODE_OPTIONAL_DROPDOWN:
            case UE_RADIO_MULTIPLE_CODE:
            case UE_DROPDOWN:
                return "ue-prognos";
            case UE_CHECKBOX_DATE_RANGE_LIST:
                return "ue-sjukskrivningar";
            case UE_ICF:
                return "ue-icf";
            case CATEGORY:
                return "ue-kategori";
            case UE_DATE:
                return "ue-date";
            case UE_TEXTFIELD:
                return "ue-textfield";
            case UE_TYPE_AHEAD:
                return "ue-typeahead";
            case UE_CAUSE_OF_DEATH:
                return "ue-cause-of-death";
            case UE_CAUSE_OF_DEATH_LIST:
                return "ue-cause-of-death-list";
            case UE_MEDICAL_INVESTIGATION:
                return "ue-medical-investigation";
            case UE_VISUAL_ACUITY:
                return "ue-visual-acuity";
            case UE_INTEGER:
                return "ue-integer";
            case UE_DATE_RANGE:
                return "ue-date-range";
            default:
                throw new RuntimeException("No conversion specified for componentType: " + componentType);
        }
    }

    private boolean isQuestion40InFK7804(String certificateType, String questionId) {
        return certificateType.equals("lisjp") && questionId.equals("40");
    }

}