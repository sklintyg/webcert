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

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;

import java.util.List;


/**
 * @author andreaskaltenbach
 */
public class LakarutlatandeEnkelTypeValidator {

    private LakarutlatandeEnkelType lakarutlatandeEnkelType;
    private List<String> validationErrors;

    public LakarutlatandeEnkelTypeValidator(LakarutlatandeEnkelType lakarutlatande, List<String> validationErrors) {
        this.lakarutlatandeEnkelType = lakarutlatande;
        this.validationErrors = validationErrors;
    }

    public void validateAndCorrect() {
        if (lakarutlatandeEnkelType == null) {
            validationErrors.add("No lakarutlatande found!");
        } else {
            if (lakarutlatandeEnkelType.getLakarutlatandeId() == null || lakarutlatandeEnkelType.getLakarutlatandeId().isEmpty()) {
                validationErrors.add("No Lakarutlatande Id found!");
            }

            if (lakarutlatandeEnkelType.getSigneringsTidpunkt() == null) {
                validationErrors.add("No signeringstidpunkt found!");
            }
            // validateAndCorrectPatient();
        }
    }

    // INTYG-4086
//    private void validateAndCorrectPatient() {
//        PatientType patient = lakarutlatandeEnkelType.getPatient();
//        if (PatientValidator.validateAndCorrect(lakarutlatandeEnkelType.getLakarutlatandeId(), patient, validationErrors)) {
//            // Get namn for patient - mandatory
//            // As per INTYG-4086, name is intentionally left out.
////            if (patient.getFullstandigtNamn() == null || patient.getFullstandigtNamn().isEmpty()) {
////                validationErrors.add("No Patient fullstandigtNamn elements found or set!");
////            }
//        }
//    }

}
