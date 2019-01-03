/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.auth;

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class WcAuthorityAsserter implements AuthorityAsserter {

    private static final String AUTH_MSG = "User missing required privilege or cannot handle sekretessmarkerad patient";
    private static final String PU_MSG = "Could not fetch sekretesstatus for patient from PU service";

    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;

    public WcAuthorityAsserter(final WebCertUserService webCertUserService, final PatientDetailsResolver patientDetailsResolver) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
    }

    @Override
    public void assertIsAuthorized(final Personnummer personnummer, final String authority) {

        final WebCertUser user = webCertUserService.getUser();

        authoritiesValidator.given(user)
                .privilege(authority)
                .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING, AUTH_MSG));

        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, PU_MSG);
        }

        if (sekretessStatus == SekretessStatus.TRUE) {
            authoritiesValidator.given(user)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING, AUTH_MSG));
        }
    }
}
