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
package se.inera.intyg.webcert.web.integration.validator;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidator {

    // Static config of the only types of intyg that allows creation when patient is actually dead.
    // Potentially this could have been implemented as a feature, but it was considered to be more of a long-lived
    // business requirement, without the need of being easily toggled.
    private static final List<String> AVLIDEN_PATIENT_ALLOWED_FOR_TYPES = Arrays.asList(
            DbModuleEntryPoint.MODULE_ID,
            DoiModuleEntryPoint.MODULE_ID);

    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;


    protected void validateBusinessRulesForSekretessmarkeradPatient(ResultValidator errors, String intygsTyp, String personnummer,
            IntygUser user) {

        Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personnummer).orElse(null);

        if (pnr != null) {
            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(pnr);
            if (sekretessStatus != SekretessStatus.UNDEFINED) {
                validateSekretess(errors, intygsTyp, sekretessStatus);
                validateHsaUserMayCreateDraft(errors, user, sekretessStatus);
            } else {
                errors.addError("Cannot issue intyg. The PU-service was unreachable. Please try again later.");
            }
        }
    }

    protected void validateCreateForAvlidenPatientAllowed(ResultValidator errors, String personId, String typAvUtlatande) {
        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvUtlatande);
        Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personId).orElse(null);

        if (pnr != null) {
            if (patientDetailsResolver.isAvliden(pnr) && !AVLIDEN_PATIENT_ALLOWED_FOR_TYPES.contains(intygsTyp)) {
                errors.addError("Cannot issue intyg type {0} for deceased patient", intygsTyp);
            }
        } else {
            errors.addError("Cannot issue intyg type {0} for patient with invalid personnummer {1}", intygsTyp, personId);
        }
    }

    protected void  validatePersonnummer(ResultValidator errors, String personId) {
        Personnummer pnr = new Personnummer(personId);
        PersonnummerChecksumValidator.validate(pnr, errors);
    }

    protected void  validatePersonnummerExists(ResultValidator errors, String personId) {
        Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personId).orElse(null);
        PersonSvar personSvar = patientDetailsResolver.getPersonFromPUService(pnr);

        switch (personSvar.getStatus()) {
            case NOT_FOUND:
                String msg = "Personnumret du har angivit finns inte i folkbokföringsregistret."
                        + " Observera att det inte går att ange reservnummer."
                        + " Webcert hanterar enbart person- och samordningsnummer.";
                errors.addError(msg);
                break;
            default:
                break; // Do nothing
        }
    }

    private void validateHsaUserMayCreateDraft(ResultValidator errors, IntygUser user, SekretessStatus sekretessStatus) {
        if (sekretessStatus == SekretessStatus.TRUE) {
            AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
            if (!authoritiesValidator.given(user)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified()) {
                errors.addError(
                        "Du saknar behörighet. För att hantera intyg för patienter med sekretessmarkering krävs "
                                + "att du har befattningen läkare eller tandläkare");
            }
        }
    }

    private void validateSekretess(ResultValidator errors, String intygsTyp, SekretessStatus sekretessStatus) {

        if (!commonAuthoritiesResolver.getSekretessmarkeringAllowed().contains(intygsTyp)) {

            switch (sekretessStatus) {
            case TRUE:
                errors.addError("Cannot issue intyg type {0} for patient having "
                        + "sekretessmarkering.", intygsTyp);
                break;
            case UNDEFINED:
                errors.addError("Cannot issue intyg type {0} for unknown patient. Might be due "
                        + "to a problem in the PU service.", intygsTyp);
                break;
            case FALSE:
                break; // Do nothing
            }
        }
    }

}
