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
package se.inera.intyg.webcert.web.service.facade.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.patient.GetPatientFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.facade.dto.PatientResponseDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetPatientFacadeServiceImplTest {

    @Mock
    private PUService puService;
    @Mock
    private MonitoringLogService monitoringService;

    @InjectMocks
    private GetPatientFacadeServiceImpl getPatientFacadeService;

    private final static String PATIENT_ID = "191212121212";
    private final static String FIRSTNAME = "firstname";
    private final static String LASTNAME = "lastname";
    private final static String MIDDLENAME = "middlename";


    void setupPatient(boolean protectedPerson, boolean testIndicated, boolean deceased, boolean includeMiddleName) {
        PersonSvar personSvar = createPersonSvar(protectedPerson, testIndicated, deceased, includeMiddleName);
        doReturn(personSvar).when(puService).getPerson(any());
    }

    void setupPatient(boolean protectedPerson, boolean testIndicated, boolean deceased) {
        setupPatient(protectedPerson, testIndicated, deceased, true);

    }

    void setupPatient() {
        setupPatient(false, false, false, true);
    }

    void setupPatientWithoutMiddleName() {
        setupPatient(false, false, false, false);
    }

    @Test
    void shallGetPatientWithPatientId() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(PATIENT_ID, response.getPatient().getPersonId().getId());
    }

    @Test
    void shallGetPatientWithFirstName() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME, response.getPatient().getFirstName());
    }

    @Test
    void shallGetPatientWithLastName() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(LASTNAME, response.getPatient().getLastName());
    }

    @Test
    void shallGetPatientWithMiddleName() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(MIDDLENAME, response.getPatient().getMiddleName());
    }

    @Test
    void shallSetFullNameForPatientWithMiddleName() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME + " " + MIDDLENAME + " " + LASTNAME, response.getPatient().getFullName());
    }

    @Test
    void shallSetFullNameForPatientWithoutMiddleName() {
        setupPatientWithoutMiddleName();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME + " " + LASTNAME, response.getPatient().getFullName());
    }

    @Test
    void shallGetPatientWithProtectedPersonFlag() {
        setupPatient(true, false, false);

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(response.getPatient().isProtectedPerson());
        assertFalse(response.getPatient().isDeceased());
        assertFalse(response.getPatient().isTestIndicated());
    }

    @Test
    void shallGetPatientWithDeceasedFlag() {
        setupPatient(false, false, true);

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(response.getPatient().isDeceased());
        assertFalse(response.getPatient().isProtectedPerson());
        assertFalse(response.getPatient().isTestIndicated());
    }

    @Test
    void shallGetPatientWithAllFlags() {
        setupPatient(true, true, true);

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(response.getPatient().isTestIndicated());
        assertTrue(response.getPatient().isDeceased());
        assertTrue(response.getPatient().isProtectedPerson());
    }

    @Test
    void shallGetPatientWithNoFlags() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        assertFalse(response.getPatient().isTestIndicated());
        assertFalse(response.getPatient().isDeceased());
        assertFalse(response.getPatient().isProtectedPerson());
    }

    @Test
    void shallLogPULookup() {
        setupPatient();

        final var response = getPatientFacadeService.getPatient(PATIENT_ID);

        verify(monitoringService).logPULookup(Personnummer.createPersonnummer(PATIENT_ID).get(), response.getStatus().name());
    }

    private PersonSvar createPersonSvar(boolean protectedPerson, boolean testIndicated, boolean deceased, boolean includeMiddleName) {
        Person person = new Person(Personnummer.createPersonnummer(PATIENT_ID).get(), protectedPerson, deceased, FIRSTNAME, includeMiddleName ? MIDDLENAME : null, LASTNAME, "", "", "", testIndicated);
        return PersonSvar.found(person);
    }
}
