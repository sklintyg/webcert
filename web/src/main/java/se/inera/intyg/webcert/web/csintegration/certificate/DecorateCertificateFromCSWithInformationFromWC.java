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

package se.inera.intyg.webcert.web.csintegration.certificate;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@Slf4j
@RequiredArgsConstructor
@Service
public class DecorateCertificateFromCSWithInformationFromWC {

    private static final String PERSON_ID_TYPE = "PERSON_NUMMER";
    private final WebCertUserService webCertUserService;

    public void decorate(Certificate certificate) {
        final var user = webCertUserService.getUser();

        if (user == null || user.getParameters() == null) {
            return;
        }

        final var parameters = user.getParameters();

        final var patient = decoratePatient(parameters, certificate.getMetadata().getPatient());
        certificate.getMetadata().setPatient(patient);
    }

    private Patient decoratePatient(IntegrationParameters parameters, Patient patient) {
        return patient
            .withPersonId(getPersonId(parameters, patient))
            .withPreviousPersonId(getPreviousPersonId(parameters, patient))
            .withPersonIdChanged(isPatientIdChanged(parameters, patient))
            .withDifferentNameFromEHR(isPatientNameDifferent(parameters, patient))
            .withReserveId(hasReserveId(parameters));
    }

    private PersonId getPreviousPersonId(IntegrationParameters parameters, Patient patient) {
        if (isBeforeAlternateSSNSet(parameters)) {
            final var personnummer = Personnummer.createPersonnummer(parameters.getBeforeAlternateSsn()).orElse(null);
            return PersonId.builder()
                .id(personnummer != null ? personnummer.getPersonnummerWithDash() : parameters.getBeforeAlternateSsn())
                .type(PERSON_ID_TYPE)
                .build();
        }

        if (isAlternateSSNSet(parameters)) {
            return PersonId.builder()
                .id(patient.getPersonId().getId())
                .type(PERSON_ID_TYPE)
                .build();
        }

        return null;
    }

    private PersonId getPersonId(IntegrationParameters parameters, Patient patient) {
        final var patientId = Personnummer.createPersonnummer(patient.getPersonId().getId()).orElse(null);
        final var id = !isAlternateSSNSet(parameters)
            || isPersonIdSameAsAlternateSSN(patientId, parameters)
            ? Objects.requireNonNull(patientId).getPersonnummerWithDash()
            : parameters.getAlternateSsn();
        return PersonId.builder()
            .id(id)
            .type(PERSON_ID_TYPE)
            .build();
    }

    private boolean isPatientIdChanged(IntegrationParameters parameters, Patient patient) {
        final var personId = isBeforeAlternateSSNSet(parameters)
            ? Personnummer.createPersonnummer(parameters.getBeforeAlternateSsn())
            : Personnummer.createPersonnummer(patient.getPersonId().getId());

        return personId.filter(personnummer -> isAlternateSSNSet(parameters)
            && !isPersonIdSameAsAlternateSSN(personnummer, parameters)
            && isValidPersonIdOrCoordinationId(parameters.getAlternateSsn())).isPresent();
    }

    private boolean isPatientNameDifferent(IntegrationParameters parameters, Patient patient) {
        return isNameSentAsParameter(parameters)
            && isNameDifferent(patient, parameters);
    }

    private boolean isNameDifferent(Patient patient, IntegrationParameters parameters) {
        final var isFirstNameDifferent = isStringDifferent(patient.getFirstName(), parameters.getFornamn());
        final var isLastNameDifferent = isStringDifferent(parameters.getEfternamn(), patient.getLastName());
        return isFirstNameDifferent || isLastNameDifferent;
    }

    private boolean isStringDifferent(String s1, String s2) {
        return s1 == null || !s1.equals(s2);
    }

    private boolean isNameSentAsParameter(IntegrationParameters parameters) {
        return parameters != null && parameters.getFornamn() != null && parameters.getEfternamn() != null;
    }

    private boolean isBeforeAlternateSSNSet(IntegrationParameters parameters) {
        return parameters != null && parameters.getBeforeAlternateSsn() != null && !parameters.getBeforeAlternateSsn().isEmpty();
    }

    private boolean isAlternateSSNSet(IntegrationParameters parameters) {
        return parameters != null && parameters.getAlternateSsn() != null && !parameters.getAlternateSsn().isEmpty();
    }

    private boolean isPersonIdSameAsAlternateSSN(Personnummer patientId, IntegrationParameters parameters) {
        return parameters != null && compareWithAndWithoutDash(patientId, parameters.getAlternateSsn());
    }

    private boolean compareWithAndWithoutDash(Personnummer patientId, String patientIdAsString) {
        return patientId.getPersonnummer().equals(patientIdAsString)
            || patientId.getPersonnummerWithDash().equals(patientIdAsString);
    }

    private boolean isValidPersonIdOrCoordinationId(String id) {
        return Personnummer.createPersonnummer(id).isPresent();
    }

    private boolean hasReserveId(IntegrationParameters parameters) {
        return isAlternateSSNSet(parameters)
            && !isValidPersonIdOrCoordinationId(parameters.getAlternateSsn());
    }
}
