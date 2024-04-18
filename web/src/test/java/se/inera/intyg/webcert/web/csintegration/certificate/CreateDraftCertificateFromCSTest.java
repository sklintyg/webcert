/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@ExtendWith(MockitoExtension.class)
class CreateDraftCertificateFromCSTest {

    private static final String INVALID_PERSON_ID = "INVALID";
    private static final String HSA_ID = "HSA_ID";
    private static final String VALID_PERSON_ID = "191212121212";

    @Mock
    PatientDetailsResolver patientDetailsResolver;
    @InjectMocks
    CreateDraftCertificateFromCS createDraftCertificateFromCS;

    @Test
    void shouldThrowIfPersonIdCouldNotBeCreated() {
        final var certificate = getCertificate(INVALID_PERSON_ID);

        final var illegalArgumentException = assertThrows(IllegalArgumentException.class,
            () -> createDraftCertificateFromCS.create(certificate, new IntygUser(HSA_ID)));

        assertTrue(illegalArgumentException.getMessage().contains("Cannot create Personnummer object with invalid personId"));
    }

    @Test
    void shouldReturnErrorResponseIfSekretessStatusIsUndefined() {
        final var certificate = getCertificate(VALID_PERSON_ID);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);

        final var result = createDraftCertificateFromCS.create(certificate, new IntygUser(HSA_ID));

        assertTrue(result.getResult().getResultText().contains("Information om skyddade personuppgifter kunde inte hämtas."));
        assertEquals(ErrorIdType.APPLICATION_ERROR, result.getResult().getErrorId());
    }

    private static Intyg getCertificate(String extension) {
        final var certificate = new Intyg();
        certificate.setPatient(new Patient());
        certificate.getPatient().setPersonId(new PersonId());
        certificate.getPatient().getPersonId().setExtension(extension);
        return certificate;
    }
}