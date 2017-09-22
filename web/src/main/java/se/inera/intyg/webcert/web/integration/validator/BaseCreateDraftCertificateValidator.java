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
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidator {

    @Autowired
    protected WebcertUserDetailsService webcertUserDetailsService;
    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;
    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    protected void validateBusinessRulesForSekretessmarkeradPatient(ResultValidator errors, String intygsTyp, String personnummer,
            IntygUser user) {

        Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personnummer).orElse(null);

        if (pnr != null) {
            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(pnr);
            validateSekretess(errors, intygsTyp, sekretessStatus);
            validateHsaUserMayCreateDraft(errors, user, sekretessStatus);
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
