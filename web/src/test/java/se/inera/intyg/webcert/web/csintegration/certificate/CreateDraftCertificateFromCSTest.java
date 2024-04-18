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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@ExtendWith(MockitoExtension.class)
class CreateDraftCertificateFromCSTest {

    private static final String INVALID_PERSON_ID = "INVALID";
    private static final String HSA_ID = "HSA_ID";
    private static final String VALID_PERSON_ID = "191212121212";
    private static final String CERTIFICATE_TYPE = "certificateType";

    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private CreateDraftCertificateFromCS createDraftCertificateFromCS;

    @Test
    void shouldThrowIfPersonIdCouldNotBeCreated() {
        final var certificate = getCertificate(INVALID_PERSON_ID);
        final var user = new IntygUser(HSA_ID);
        final var illegalArgumentException = assertThrows(IllegalArgumentException.class,
            () -> createDraftCertificateFromCS.create(certificate, user));

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

    @Test
    void shouldReturnNullIfCertificateTypeDontExists() {
        final var certificate = getCertificate(VALID_PERSON_ID);
        when(csIntegrationService.certificateTypeExists(certificate.getTypAvIntyg().getCode())).thenReturn(Optional.empty());
        assertNull(createDraftCertificateFromCS.create(certificate, new IntygUser(HSA_ID)));
    }

    @Test
    void shouldReturnCreateDraftCertificateResponseType() {
        final var expectedResult = new CreateDraftCertificateResponseType();
        final var certificate = getCertificate(VALID_PERSON_ID);
        final var user = new IntygUser(HSA_ID);
        final var modelIdDTO = CertificateModelIdDTO.builder().build();
        final var request = CreateCertificateRequestDTO.builder().build();

        when(csIntegrationService.certificateTypeExists(certificate.getTypAvIntyg().getCode()))
            .thenReturn(Optional.of(modelIdDTO));
        when(csIntegrationRequestFactory.createDraftCertificateRequest(modelIdDTO, certificate, user)).thenReturn(request);
        when(csIntegrationService.createDraftCertificate(request)).thenReturn(expectedResult);

        final var result = createDraftCertificateFromCS.create(certificate, user);
        assertEquals(expectedResult, result);
    }

    private static Intyg getCertificate(String extension) {
        final var certificate = new Intyg();
        certificate.setPatient(new Patient());
        certificate.getPatient().setPersonId(new PersonId());
        certificate.getPatient().getPersonId().setExtension(extension);
        certificate.setTypAvIntyg(new TypAvIntyg());
        certificate.getTypAvIntyg().setCode(CERTIFICATE_TYPE);
        return certificate;
    }
}
