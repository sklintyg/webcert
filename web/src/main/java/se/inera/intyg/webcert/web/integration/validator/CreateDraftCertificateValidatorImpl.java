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

package se.inera.intyg.webcert.web.integration.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;

import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;


@Component
public class CreateDraftCertificateValidatorImpl implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.integration.validator.CreateDraftCertificateValidator#validate(se.inera.certificate.clinicalprocess
     * .healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType)
     */
    @Override
    public ResultValidator validate(Utlatande utlatande) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvUtlatande(utlatande.getTypAvUtlatande(), errors);
        validatePatient(utlatande.getPatient(), errors);
        validateSkapadAv(utlatande.getSkapadAv(), errors);

        return errors;
    }

    public void validateTypAvUtlatande(TypAvUtlatande typAvUtlatandeType, ResultValidator errors) {
        String intygsTyp = typAvUtlatandeType.getCode();

        if (!moduleRegistry.moduleExists(intygsTyp)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void validatePatient(Patient patient, ResultValidator errors) {
        if (StringUtils.isBlank(patient.getEfternamn())) {
            errors.addError("efternamn is required");
        }

        if ((patient.getFornamn() == null) || patient.getFornamn().isEmpty()) {
            errors.addError("At least one fornamn is required");
        }
    }

    private void validateSkapadAv(HosPersonal skapadAv, ResultValidator errors) {
        if (StringUtils.isBlank(skapadAv.getFullstandigtNamn())) {
            errors.addError("Physicians full name is required");
        }

        validateEnhet(skapadAv.getEnhet(), errors);
    }

    private void validateEnhet(Enhet enhet, ResultValidator errors) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else if (StringUtils.isBlank(enhet.getEnhetsnamn())) {
            errors.addError("enhetsnamn is required");
        }
    }

}
