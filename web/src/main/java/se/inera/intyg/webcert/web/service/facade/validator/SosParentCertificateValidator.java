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

package se.inera.intyg.webcert.web.service.facade.validator;

import java.util.List;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueText;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadEnum;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;

public class SosParentCertificateValidator implements CertificateValidator {

    private static final String DODSDATUM_DODSPLATS_CATEGORY_ID = "dodsdatumDodsplats";
    private static final String QUESTION_DODSPLATS_KOMMUN = "3.1";
    private static final String VALIDATION_MESSAGE = "Du måste välja ett alternativ.";

    private final DefaultTypeAheadProvider typeAheadProvider;

    public SosParentCertificateValidator(DefaultTypeAheadProvider typeAheadProvider) {
        this.typeAheadProvider = typeAheadProvider;
    }

    @Override
    public void validate(Certificate certificate, DraftValidation draftValidation) {
        validateDodsplatsKommun(certificate, draftValidation);
    }

    private void validateDodsplatsKommun(Certificate certificate, DraftValidation draftValidation) {
        final var certificateData = certificate.getData();

        if (!certificateData.containsKey(QUESTION_DODSPLATS_KOMMUN)) {
            return;
        }

        final var dataElement = certificateData.get(QUESTION_DODSPLATS_KOMMUN);
        if (dataElement == null || dataElement.getValue() == null) {
            return;
        }

        if (!(dataElement.getValue() instanceof CertificateDataValueText textValue)) {
            return;
        }

        final var value = textValue.getText();

        if (value == null || value.isEmpty()) {
            return;
        }

        if (!getValidMunicipalityValues().contains(value)) {
            final var validationMessage = new DraftValidationMessage(
                DODSDATUM_DODSPLATS_CATEGORY_ID,
                QUESTION_DODSPLATS_KOMMUN,
                ValidationMessageType.INVALID_FORMAT,
                VALIDATION_MESSAGE,
                "NO_KEY",
                QUESTION_DODSPLATS_KOMMUN
            );

            draftValidation.addMessage(validationMessage);
            draftValidation.setStatus(ValidationStatus.INVALID);
        }
    }

    private List<String> getValidMunicipalityValues() {
        return typeAheadProvider.getValues(TypeAheadEnum.MUNICIPALITIES);
    }
}
