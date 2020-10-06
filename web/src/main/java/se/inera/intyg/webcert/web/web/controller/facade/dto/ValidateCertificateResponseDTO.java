package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;

public class ValidateCertificateResponseDTO {

    private ValidationErrorDTO[] validationErrors;

    public static ValidateCertificateResponseDTO create(ValidationErrorDTO[] validationErrors) {
        final ValidateCertificateResponseDTO responseDTO = new ValidateCertificateResponseDTO();
        responseDTO.validationErrors = validationErrors;
        return responseDTO;
    }

    public ValidationErrorDTO[] getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(ValidationErrorDTO[] validationErrors) {
        this.validationErrors = validationErrors;
    }
}
