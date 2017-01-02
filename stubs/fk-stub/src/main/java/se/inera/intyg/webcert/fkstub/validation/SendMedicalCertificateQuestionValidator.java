/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.fkstub.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;

public class SendMedicalCertificateQuestionValidator {
    private final QuestionToFkType questionType;
    private final List<String> validationErrors = new ArrayList<>();

    public SendMedicalCertificateQuestionValidator(QuestionToFkType questionType) {
        this.questionType = questionType;
    }

    public void validateAndCorrect() {
        // First, validate properties at SendMedicalCertificateQuestion request level
        if (StringUtils.isEmpty(questionType.getVardReferensId())) {
            validationErrors.add("No vardReferens-id found!");
        }
        if (questionType.getAmne() == null) {
            validationErrors.add("No Amne element found!");
        }
        if (questionType.getFraga() == null) {
            validationErrors.add("No Question fraga element found!");
        } else {
            if (StringUtils.isEmpty(questionType.getFraga().getMeddelandeText())) {
                validationErrors.add("No Question fraga meddelandeText elements found or set!");
            }
            if (questionType.getFraga().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Question fraga signeringsTidpunkt elements found or set!");
            }
        }
        if (questionType.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(questionType.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(questionType.getAdressVard(), validationErrors).validateAndCorrect();

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }
}
