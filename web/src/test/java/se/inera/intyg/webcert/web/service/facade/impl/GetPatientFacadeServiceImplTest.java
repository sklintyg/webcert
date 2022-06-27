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
import se.inera.intyg.webcert.web.service.facade.patient.InvalidPatientIdException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientNoNameException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientSearchErrorException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import static org.junit.jupiter.api.Assertions.*;
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
    void shallGetPatientWithPatientId() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(PATIENT_ID, patient.getPersonId().getId());
    }

    @Test
    void shallGetPatientWithFirstName() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME, patient.getFirstName());
    }

    @Test
    void shallGetPatientWithLastName() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(LASTNAME, patient.getLastName());
    }

    @Test
    void shallGetPatientWithMiddleName() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(MIDDLENAME, patient.getMiddleName());
    }

    @Test
    void shallSetFullNameForPatientWithMiddleName() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME + " " + MIDDLENAME + " " + LASTNAME, patient.getFullName());
    }

    @Test
    void shallSetFullNameForPatientWithoutMiddleName() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatientWithoutMiddleName();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertEquals(FIRSTNAME + " " + LASTNAME, patient.getFullName());
    }

    @Test
    void shallGetPatientWithProtectedPersonFlag() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient(true, false, false);

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(patient.isProtectedPerson());
        assertFalse(patient.isDeceased());
        assertFalse(patient.isTestIndicated());
    }

    @Test
    void shallGetPatientWithDeceasedFlag() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient(false, false, true);

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(patient.isDeceased());
        assertFalse(patient.isProtectedPerson());
        assertFalse(patient.isTestIndicated());
    }

    @Test
    void shallGetPatientWithAllFlags() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient(true, true, true);

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertTrue(patient.isTestIndicated());
        assertTrue(patient.isDeceased());
        assertTrue(patient.isProtectedPerson());
    }

    @Test
    void shallGetPatientWithNoFlags() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);

        assertFalse(patient.isTestIndicated());
        assertFalse(patient.isDeceased());
        assertFalse(patient.isProtectedPerson());
    }

    @Test
    void shallLogPULookupIfPatientIsFound() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatient();

        getPatientFacadeService.getPatient(PATIENT_ID);

        verify(monitoringService).logPULookup(Personnummer.createPersonnummer(PATIENT_ID).get(), "FOUND");
    }

    @Test
    void shallLogPULookupIfPatientIsNotFound() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatientNotFound();

        getPatientFacadeService.getPatient(PATIENT_ID);

        verify(monitoringService).logPULookup(Personnummer.createPersonnummer(PATIENT_ID).get(), "NOT_FOUND");
    }

    @Test
    void shallLogPULookupIfPUError() {
        setupPatientError();
        try {
            getPatientFacadeService.getPatient(PATIENT_ID);
        } catch (InvalidPatientIdException | PatientSearchErrorException | PatientNoNameException e) {
            e.printStackTrace();
        }

        verify(monitoringService).logPULookup(Personnummer.createPersonnummer(PATIENT_ID).get(), "ERROR");
    }

    @Test
    void shallReturnNullIfPersonIsNotFound() throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
        setupPatientNotFound();
        final var patient = getPatientFacadeService.getPatient(PATIENT_ID);
        assertNull(patient);
    }

    @Test
    void shallThrowExceptionIfPUError() {
        setupPatientError();
        assertThrows(PatientSearchErrorException.class, () -> getPatientFacadeService.getPatient(PATIENT_ID));
    }

    @Test
    void shallThrowExceptionIfPatientHasNoFirstName() {
        final var patient = new Person(Personnummer.createPersonnummer("191212121212").get(), false, false, null, "name", "name", "", "", "");
        doReturn(PersonSvar.found(patient)).when(puService).getPerson(any());
        assertThrows(PatientNoNameException.class, () -> getPatientFacadeService.getPatient(PATIENT_ID));
    }

    @Test
    void shallThrowExceptionIfPatientHasNoLastName() {
        final var patient = new Person(Personnummer.createPersonnummer("191212121212").get(), false, false, "", "name", null, "", "", "");
        doReturn(PersonSvar.found(patient)).when(puService).getPerson(any());
        assertThrows(PatientNoNameException.class, () -> getPatientFacadeService.getPatient(PATIENT_ID));
    }

    @Test
    void shallThrowExceptionIfPatientIdIsInvalid() {
        assertThrows(InvalidPatientIdException.class, () -> getPatientFacadeService.getPatient("121212"));
    }

    private PersonSvar createPersonSvar(boolean protectedPerson, boolean testIndicated, boolean deceased, boolean includeMiddleName) {
        Person person = new Person(Personnummer.createPersonnummer(PATIENT_ID).get(), protectedPerson, deceased, FIRSTNAME, includeMiddleName ? MIDDLENAME : null, LASTNAME, "", "", "", testIndicated);
        return PersonSvar.found(person);
    }

    private void setupPatientNotFound() {
        doReturn(PersonSvar.notFound()).when(puService).getPerson(any());
    }

    private void setupPatientError() {
        doReturn(PersonSvar.error()).when(puService).getPerson(any());
    }
}
