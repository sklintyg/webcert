/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@Component
public class PatientConverterImpl implements PatientConverter {

    private final PatientDetailsResolver patientDetailsResolver;

    private final WebCertUserService webCertUserService;

    @Autowired
    public PatientConverterImpl(PatientDetailsResolver patientDetailsResolver, WebCertUserService webCertUserService) {
        this.patientDetailsResolver = patientDetailsResolver;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public Patient convert(Personnummer patientId, String certificateType, String certificateTypeVersion) {
        final var patient = patientDetailsResolver.resolvePatient(patientId, certificateType, certificateTypeVersion);
        final var user = webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser() : null;
        final var parameters = getIntegrationParameters(user);

        return Patient.builder()
            .personId(
                getPersonId(patientId, parameters)
            )
            .firstName(patient.getFornamn())
            .middleName(patient.getMellannamn())
            .lastName(patient.getEfternamn())
            .fullName(patient.getFullstandigtNamn())
            .testIndicated(patient.isTestIndicator())
            .protectedPerson(isProtectedPerson(patientId))
            .deceased(patient.isAvliden())
            .differentNameFromEHR(isPatientNameDifferent(patient, parameters))
            .previousPersonId(getPreviousPersonId(patientId, parameters))
            .personIdUpdated(isPatientIdUpdated(parameters, patientId))
            .build();
    }

    private boolean isPatientIdUpdated(IntegrationParameters parameters, Personnummer patientId) {
        return isBeforeAlternateSSNSet(parameters) && !isPersonIdSameAsBeforeAlternateSSN(patientId, parameters);
    }

    private boolean isBeforeAlternateSSNSet(IntegrationParameters parameters) {
        return parameters != null && parameters.getBeforeAlternateSsn() != null && !parameters.getBeforeAlternateSsn().equals("");
    }

    private PersonId getPersonId(Personnummer patientId, IntegrationParameters parameters) {
        final var id = parameters == null || !isAlternateSSNSet(parameters)
            || isPersonIdSameAsAlternateSSN(patientId, parameters)
            ? patientId.getPersonnummerWithDash()
            : parameters.getAlternateSsn();
        return PersonId.builder()
            .id(id)
            .type("PERSON_NUMMER")
            .build();
    }

    private boolean isPersonIdSameAsBeforeAlternateSSN(Personnummer patientId, IntegrationParameters parameters) {
        return parameters != null && compareWithAndWithoutDash(patientId, parameters.getBeforeAlternateSsn());
    }

    private boolean isPersonIdSameAsAlternateSSN(Personnummer patientId, IntegrationParameters parameters) {
        return parameters != null && compareWithAndWithoutDash(patientId, parameters.getAlternateSsn());
    }

    private boolean compareWithAndWithoutDash(Personnummer patientId, String patientIdAsString) {
        return patientId.getPersonnummer().equals(patientIdAsString)
            || patientId.getPersonnummerWithDash().equals(patientIdAsString);
    }

    private boolean isPatientNameDifferent(se.inera.intyg.common.support.model.common.internal.Patient patient,
        IntegrationParameters parameters) {
        return isNameSentAsParameter(parameters)
            && isNameDifferent(patient, parameters);
    }

    private boolean isNameDifferent(se.inera.intyg.common.support.model.common.internal.Patient patient, IntegrationParameters parameters) {
        final var isFirstNameDifferent = isStringDifferent(patient.getFornamn(), parameters.getFornamn());
        final var isLastNameDifferent = isStringDifferent(parameters.getEfternamn(), patient.getEfternamn());
        return isFirstNameDifferent || isLastNameDifferent;
    }

    private boolean isStringDifferent(String s1, String s2) {
        return !s1.equals(s2);
    }

    private boolean isNameSentAsParameter(IntegrationParameters parameters) {
        return parameters != null && parameters.getFornamn() != null && parameters.getEfternamn() != null;
    }

    private PersonId getPreviousPersonId(Personnummer patientId, IntegrationParameters parameters) {
        if (isPatientIdNotChanged(patientId, parameters)) {
            return null;
        } else if (!isBeforeAlternateSSNSet(parameters)) {
            return PersonId.builder()
                .id(patientId.getPersonnummerWithDash())
                .type("PERSON_NUMMER")
                .build();
        }
        return PersonId.builder()
            .id(parameters.getBeforeAlternateSsn())
            .type("PERSON_NUMMER")
            .build();
    }

    private boolean isPatientIdNotChanged(Personnummer patientId, IntegrationParameters parameters) {
        return parameters == null
            || (isPersonIdSameAsAlternateSSN(patientId, parameters) && !isPatientIdUpdated(parameters, patientId))
            || !isAlternateSSNSet(parameters);
    }

    private boolean isAlternateSSNSet(IntegrationParameters parameters) {
        return parameters != null && parameters.getAlternateSsn() != null && !parameters.getAlternateSsn().equals("");
    }

    private boolean isProtectedPerson(Personnummer patientId) {
        final var protectedStatus = patientDetailsResolver.getSekretessStatus(patientId);
        return protectedStatus == SekretessStatus.TRUE || protectedStatus == SekretessStatus.UNDEFINED;
    }

    private IntegrationParameters getIntegrationParameters(WebCertUser user) {
        if (user == null) {
            return null;
        }
        return user.getParameters();
    }
}
