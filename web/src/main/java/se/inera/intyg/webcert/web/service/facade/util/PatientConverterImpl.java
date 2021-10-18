/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
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
    public Patient convert(Utkast certificate) {
        final var patientId = certificate.getPatientPersonnummer();
        final var user = webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser() : null;
        final var parameters = getIntegrationParameters(user);

        return Patient.builder()
            .personId(
                getPersonId(patientId, parameters)
            )
            .firstName(certificate.getPatientFornamn())
            .middleName(certificate.getPatientMellannamn())
            .lastName(certificate.getPatientEfternamn())
            .fullName(getFullName(certificate))
            .testIndicated(patientDetailsResolver.isTestIndicator(patientId))
            .protectedPerson(getProtectedStatus(patientId))
            .deceased(patientDetailsResolver.isAvliden(patientId))
            .differentNameFromEHR(isPatientNameDifferent(certificate, parameters))
            .previousPersonId(getPreviousPersonId(patientId, parameters))
            .personIdUpdated(isBeforeAlternateSSNSet(parameters))
            .build();
    }

    private boolean isBeforeAlternateSSNSet(IntegrationParameters parameters) {
        return parameters != null && parameters.getBeforeAlternateSsn() != null && parameters.getBeforeAlternateSsn() != "";
    }

    private PersonId getPersonId(Personnummer patientId, IntegrationParameters parameters) {
        final var id =
            parameters == null || parameters.getAlternateSsn() == null || isPersonIdSameAsAlternateSSN(patientId, parameters)
                ? patientId.getPersonnummer()
                : parameters.getAlternateSsn();
        return PersonId.builder()
            .id(id)
            .type("PERSON_NUMMER")
            .build();
    }

    private boolean isPersonIdSameAsAlternateSSN(Personnummer patientId, IntegrationParameters parameters) {
        return parameters != null && patientId.getPersonnummer().equals(parameters.getAlternateSsn());
    }

    private boolean isPatientNameDifferent(Utkast certificate, IntegrationParameters parameters) {
        return parameters != null && parameters.getFornamn() != null && parameters.getEfternamn() != null && (
            !parameters.getFornamn().equals(certificate.getPatientFornamn()) || !parameters.getEfternamn()
                .equals(certificate.getPatientEfternamn()));
    }

    private PersonId getPreviousPersonId(Personnummer patientId, IntegrationParameters parameters) {
        if (parameters == null) {
            return null;
        } else if (isPersonIdSameAsAlternateSSN(patientId, parameters)) {
            return null;
        } else if (parameters.getBeforeAlternateSsn() != null && parameters.getBeforeAlternateSsn().equals("")) {
            return PersonId.builder()
                .id(patientId.getPersonnummer())
                .type("PERSON_NUMMER")
                .build();
        }
        return PersonId.builder()
            .id(parameters.getBeforeAlternateSsn())
            .type("PERSON_NUMMER")
            .build();
    }

    private boolean getProtectedStatus(Personnummer patientId) {
        return patientDetailsResolver.getSekretessStatus(patientId) == SekretessStatus.TRUE
            || patientDetailsResolver.getSekretessStatus(patientId) == SekretessStatus.UNDEFINED;
    }

    private String getFullName(Utkast certificate) {
        if (Objects.nonNull(certificate.getPatientMellannamn()) && certificate.getPatientMellannamn().trim().length() > 0) {
            return certificate.getPatientFornamn() + ' ' + certificate.getPatientMellannamn() + ' ' + certificate.getPatientEfternamn();
        }
        return certificate.getPatientFornamn() + ' ' + certificate.getPatientEfternamn();
    }

    private IntegrationParameters getIntegrationParameters(WebCertUser user) {
        if (user == null) {
            return null;
        }
        return user.getParameters();
    }
}
