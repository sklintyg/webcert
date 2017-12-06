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
package se.inera.intyg.webcert.web.integration.v3.validator;

import static se.inera.intyg.common.support.modules.support.feature.ModuleFeature.HANTERA_INTYGSUTKAST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.integration.validator.BaseCreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validator.IntygsTypToInternal;
import se.inera.intyg.webcert.web.integration.validator.PersonnummerChecksumValidator;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@Component(value = "createDraftCertificateValidatorImplV2")
public class CreateDraftCertificateValidatorImpl extends BaseCreateDraftCertificateValidator implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private WebcertFeatureService featureService;

    @Override
    public ResultValidator validate(Intyg intyg) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvIntyg(errors, intyg.getTypAvIntyg());
        validatePatient(errors, intyg.getPatient());
        validateSkapadAv(errors, intyg.getSkapadAv());

        return errors;
    }

    @Override
    public ResultValidator validateApplicationErrors(Intyg intyg, IntygUser user) {
        ResultValidator errors = ResultValidator.newInstance();

        String personId = intyg.getPatient().getPersonId().getExtension();
        Personnummer personnummer = Personnummer.createValidatedPersonnummerWithDash(personId).orElse(null);

        // Check if PU-service is responding
        validatePUServiceAvailibility(errors, personnummer);
        if (errors.hasErrors()) {
            return errors;
        }

        validateSekretessmarkeringOchIntygsTyp(errors, personnummer, intyg.getSkapadAv(), intyg.getTypAvIntyg(), user);
        validateCreateForAvlidenPatientAllowed(errors, personnummer, intyg.getTypAvIntyg().getCode());

        return errors;
    }

    private void validateTypAvIntyg(ResultValidator errors, TypAvIntyg typAvIntygType) {
        String intygsTyp = typAvIntygType.getCode();

        String moduleId = moduleRegistry.getModuleIdFromExternalId(intygsTyp);
        if (!moduleRegistry.moduleExists(moduleId) || !featureService.isModuleFeatureActive(HANTERA_INTYGSUTKAST.getName(),
                moduleId)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void validatePatient(ResultValidator errors, Patient patient) {
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

    private void validateSkapadAv(ResultValidator errors, HosPersonal skapadAv) {
        if (Strings.nullToEmpty(skapadAv.getFullstandigtNamn()).trim().isEmpty()) {
            errors.addError("Physicians full name is required");
        }

        validateEnhet(errors, skapadAv.getEnhet());
    }

    private void validateEnhet(ResultValidator errors, Enhet enhet) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else if (Strings.nullToEmpty(enhet.getEnhetsnamn()).trim().isEmpty()) {
            errors.addError("enhetsnamn is required");
        }
    }

    private void validateSekretessmarkeringOchIntygsTyp(ResultValidator errors,
                                                        Personnummer personnummer,
                                                        HosPersonal skapadAv,
                                                        TypAvIntyg typAvUtlatande,
                                                        IntygUser user) {

        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvUtlatande.getCode());

        if (!authoritiesValidator.given(user, intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .isVerified()) {
            errors.addError("Du saknar behörighet att skapa intyg med denna typ.");
        } else {
            validateBusinessRulesForSekretessmarkeradPatient(errors, personnummer, intygsTyp, user);
        }
    }

}
