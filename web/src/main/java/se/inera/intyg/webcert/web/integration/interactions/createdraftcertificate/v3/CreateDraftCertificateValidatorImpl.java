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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import java.util.Collections;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@Component(value = "createDraftCertificateValidatorImplV2")
public class CreateDraftCertificateValidatorImpl extends BaseCreateDraftCertificateValidator implements CreateDraftCertificateValidator {

    @Override
    public ResultValidator validate(Intyg intyg) {
        ResultValidator errors = ResultValidator.newInstance();
        validatePatient(errors, intyg.getPatient());
        validateSkapadAv(errors, intyg.getSkapadAv());
        return errors;
    }

    @Override
    public ResultValidator validateApplicationErrors(Intyg intyg, IntygUser user) {
        ResultValidator errors = ResultValidator.newInstance();
        final var personnummer = getPersonnummer(intyg, errors);
        if (errors.hasErrors()) {
            return errors;
        }
        // Check if PU-service is responding
        validatePUServiceResponse(errors, personnummer);
        return errors;
    }

    private Personnummer getPersonnummer(Intyg intyg, ResultValidator errors) {
        String personId = intyg.getPatient().getPersonId().getExtension();
        return createPersonnummer(errors, personId).orElse(null);
    }

    @Override
    public ResultValidator validateCertificateErrors(Intyg intyg, IntygUser user) {
        ResultValidator errors = ResultValidator.newInstance();

        final var personnummer = getPersonnummer(intyg, errors);
        if (errors.hasErrors()) {
            return errors;
        }

        validateTypAvIntyg(errors, intyg.getTypAvIntyg());

        if (errors.hasErrors()) {
            return errors;
        }
        
        validateSekretessmarkeringOchIntygsTyp(errors, personnummer, intyg.getTypAvIntyg(), user);
        validateCreateForAvlidenPatientAllowed(errors, personnummer, intyg.getTypAvIntyg().getCode());
        return errors;
    }

    private void validateTypAvIntyg(ResultValidator errors, TypAvIntyg typAvIntygType) {
        String intygsTyp = typAvIntygType.getCode();
        String moduleId = moduleRegistry.getModuleIdFromExternalId(intygsTyp);
        validateModuleSupport(errors, moduleId);
    }

    private void validatePatient(ResultValidator errors, Patient patient) {
        String personId = patient.getPersonId() == null ? "" : patient.getPersonId().getExtension();
        validatePatient(errors, Collections.singletonList(patient.getFornamn()), patient.getEfternamn(), personId);
    }

    private void validateSkapadAv(ResultValidator errors, HosPersonal skapadAv) {
        String personalId = skapadAv.getPersonalId() == null ? "" : skapadAv.getPersonalId().getExtension();
        validateSkapadAv(errors, skapadAv.getFullstandigtNamn(), personalId);
        validateEnhet(errors, skapadAv.getEnhet());
    }

    private void validateEnhet(ResultValidator errors, Enhet enhet) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else {
            String enhetsId = enhet.getEnhetsId() == null ? "" : enhet.getEnhetsId().getExtension();
            validateEnhet(errors, enhet.getEnhetsnamn(), enhetsId);
        }
    }

    private void validateSekretessmarkeringOchIntygsTyp(ResultValidator errors,
        Personnummer personnummer,
        TypAvIntyg typAvIntyg,
        IntygUser user) {

        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        String intygsTyp = moduleRegistry.getModuleIdFromExternalId(typAvIntyg.getCode());

        if (!authoritiesValidator.given(user, intygsTyp)
            .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
            .isVerified()) {
            errors.addError("Du saknar behörighet att skapa intyg med denna typ.");
        } else {
            validateBusinessRulesForSekretessmarkeradPatient(errors, personnummer, intygsTyp, user);
        }
    }
}
