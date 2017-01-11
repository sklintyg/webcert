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
package se.inera.intyg.webcert.web.integration.v2.validator;

import static se.inera.intyg.common.support.modules.support.feature.ModuleFeature.HANTERA_INTYGSUTKAST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.integration.validator.PersonnummerChecksumValidator;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.Patient;

@Component(value = "createDraftCertificateValidatorImplV2")
public class CreateDraftCertificateValidatorImpl implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private WebcertFeatureService featureService;

    @Override
    public ResultValidator validate(Intyg intyg) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvIntyg(intyg.getTypAvIntyg(), errors);
        validatePatient(intyg.getPatient(), errors);
        validateSkapadAv(intyg.getSkapadAv(), errors);

        return errors;
    }

    private void validateTypAvIntyg(TypAvIntyg typAvIntygType, ResultValidator errors) {
        String intygsTyp = typAvIntygType.getCode();

        String moduleId = moduleRegistry.getModuleIdFromExternalId(intygsTyp);
        if (!moduleRegistry.moduleExists(moduleId) || !featureService.isModuleFeatureActive(HANTERA_INTYGSUTKAST.getName(), moduleId)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void validatePatient(Patient patient, ResultValidator errors) {
        if (Strings.nullToEmpty(patient.getEfternamn()).trim().isEmpty()) {
            errors.addError("efternamn is required");
        }

        if (Strings.nullToEmpty(patient.getFornamn()).trim().isEmpty()) {
            errors.addError("fornamn is required");
        }

        if (patient.getPersonId() == null || Strings.nullToEmpty(patient.getPersonId().getExtension()).trim().isEmpty()) {
            errors.addError("personId is required");
        } else {
            PersonnummerChecksumValidator.validate(new Personnummer(patient.getPersonId().getExtension()), errors);
        }
    }

    private void validateSkapadAv(HosPersonal skapadAv, ResultValidator errors) {
        if (Strings.nullToEmpty(skapadAv.getFullstandigtNamn()).trim().isEmpty()) {
            errors.addError("Physicians full name is required");
        }

        validateEnhet(skapadAv.getEnhet(), errors);
    }

    private void validateEnhet(Enhet enhet, ResultValidator errors) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else if (Strings.nullToEmpty(enhet.getEnhetsnamn()).trim().isEmpty()) {
            errors.addError("enhetsnamn is required");
        }
    }
}
