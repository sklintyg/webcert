/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;

public class SendMedicalCertificateAnswerValidator {
    private final AnswerToFkType answerType;
    private final List<String> validationErrors = new ArrayList<>();

    public SendMedicalCertificateAnswerValidator(AnswerToFkType answerType) {
        this.answerType = answerType;
    }

    public void validateAndCorrect() {
        // First, validate properties at SendMedicalCertificateAnswer request level
        if (StringUtils.isEmpty(answerType.getVardReferensId())) {
            validationErrors.add("No vardReferens-id found!");
        }
        if (StringUtils.isEmpty(answerType.getFkReferensId())) {
            validationErrors.add("No fkReferens-id found!");
        }
        if (answerType.getAmne() == null) {
            validationErrors.add("No Amne element found!");
        }
        if (answerType.getFraga() == null) {
            validationErrors.add("No Answer fraga element found!");
        } else {
            if (StringUtils.isEmpty(answerType.getFraga().getMeddelandeText())) {
                validationErrors.add("No Answer fraga meddelandeText elements found or set!");
            }
            if (answerType.getFraga().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Answer fraga signeringsTidpunkt elements found or set!");
            }
        }
        if (answerType.getSvar() == null) {
            validationErrors.add("No Answer svar element found!");
        } else {
            if (StringUtils.isEmpty(answerType.getSvar().getMeddelandeText())) {
                validationErrors.add("No Answer svar meddelandeText elements found or set!");
            }
            if (answerType.getSvar().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Answer svar signeringsTidpunkt elements found or set!");
            }
        }
        if (answerType.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(answerType.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(answerType.getAdressVard(), validationErrors).validateAndCorrect();

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }
}
