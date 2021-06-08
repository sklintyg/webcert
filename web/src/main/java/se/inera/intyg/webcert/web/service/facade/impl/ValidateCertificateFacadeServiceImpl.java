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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;

@Service
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
        final var currentCertificate = utkastService.getDraft(certificate.getMetadata().getId(), false);

        final var draftValidation = utkastService.validateDraft(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            getJsonFromCertificate(certificate, currentCertificate.getModel())
        );

        return convertDraftValidation(draftValidation);
    }

    private String getJsonFromCertificate(Certificate certificate, String currentModel) {
        try {
            final var moduleApi = moduleRegistry.getModuleApi(
                certificate.getMetadata().getType(),
                certificate.getMetadata().getTypeVersion()
            );

            return moduleApi.getJsonFromCertificate(certificate, currentModel);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ValidationErrorDTO[] convertDraftValidation(DraftValidation draftValidation) {
        return draftValidation.getMessages().stream()
            .map(this::convertValidationError)
            .toArray(ValidationErrorDTO[]::new);
    }

    private ValidationErrorDTO convertValidationError(DraftValidationMessage validationMessage) {
        final var validationError = new ValidationErrorDTO();
        validationError.setCategory(validationMessage.getCategory());
        validationError.setField(validationMessage.getField());
        validationError.setType(validationMessage.getType().name());
        validationError.setId(validationMessage.getQuestionId());
        validationError.setText(
            getValidationText(validationMessage.getField())
        );
        return validationError;
    }

    // TODO: We need a way to give correct message to the user. Or should frontend adjust based on component?
    private String getValidationText(String field) {
        if (field.contains("har")) {
            return "Välj ett alternativ.";
        }
        return "Ange ett svar.";
    }
}
