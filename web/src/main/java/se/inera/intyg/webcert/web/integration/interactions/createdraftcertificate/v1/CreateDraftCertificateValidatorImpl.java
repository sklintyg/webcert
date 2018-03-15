/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v1;

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.integration.converters.IntygsTypToInternal;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;

@Component
public class CreateDraftCertificateValidatorImpl extends BaseCreateDraftCertificateValidator implements CreateDraftCertificateValidator {

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v1.CreateDraftCertificateValidator#validate(
     *      se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType)
     */
    @Override
    public ResultValidator validate(Utlatande utlatande) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvUtlatande(errors, utlatande.getTypAvUtlatande());
        validatePatient(errors, utlatande.getPatient());
        validateSkapadAv(errors, utlatande.getSkapadAv());

        return errors;
    }

    @Override
    public ResultValidator validateApplicationErrors(Utlatande utlatande, IntygUser user) {
        ResultValidator errors = ResultValidator.newInstance();

        String personId = utlatande.getPatient().getPersonId().getExtension();
        Personnummer personnummer = createPersonnummer(errors, personId).orElse(null);
        if (errors.hasErrors()) {
            return errors;
        }

        // Check if PU-service is responding
        validatePUServiceResponse(errors, personnummer);
        if (errors.hasErrors()) {
            return errors;
        }

        validateSekretessmarkeringOchIntygsTyp(errors, personnummer, utlatande.getTypAvUtlatande(), user);
        validateCreateForAvlidenPatientAllowed(errors, personnummer, utlatande.getTypAvUtlatande().getCode());

        return errors;
    }

    private void validateSekretessmarkeringOchIntygsTyp(ResultValidator errors,
            Personnummer personnummer,
            TypAvUtlatande typAvUtlatande,
            IntygUser user) {

        // If intygstyp is NOT allowed to issue for sekretessmarkerad patient
        // we check sekretessmarkerad state through the PU-service.
        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvUtlatande.getCode());

        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (!authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .isVerified()) {
            errors.addError("Du saknar behörighet att skapa intyg med denna typ.");
        } else {
            validateBusinessRulesForSekretessmarkeradPatient(errors, personnummer, intygsTyp, user);
        }
    }

    private void validateTypAvUtlatande(ResultValidator errors, TypAvUtlatande typAvUtlatandeType) {
        String moduleId = typAvUtlatandeType.getCode();
        validateModuleSupport(errors, moduleId);
    }

    private void validatePatient(ResultValidator errors, Patient patient) {
        String personId = patient.getPersonId() == null ? "" : patient.getPersonId().getExtension();
        validatePatient(errors, patient.getFornamn(), patient.getEfternamn(), personId);
    }

    private void validateSkapadAv(ResultValidator errors, HosPersonal skapadAv) {
        String personId = skapadAv.getPersonalId() == null ? "" : skapadAv.getPersonalId().getExtension();
        validateSkapadAv(errors, skapadAv.getFullstandigtNamn(), personId);
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

}
