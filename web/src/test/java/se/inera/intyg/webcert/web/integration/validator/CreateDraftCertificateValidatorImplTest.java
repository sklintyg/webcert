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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest {

    private static final String CODE = "CODE";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Before
    public void setup() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(true);
    }

    @Test
    public void testValidate() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientEfternamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, null, "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientFornamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", null, "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", null, "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "", "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientSamordningsnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19800191-0002", "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientInvalidPersonnummer() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19010101-0101", "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", null, "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHosPersonalPersonalIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", null, "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHosPersonalPersonalIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "", "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", "fullständigt namn", "enhetsnamn", false));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetsnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", "fullständigt namn", null, true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateEnhetEnhetsIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn", null, true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateEnhetEnhetsIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn", "", true));
        assertTrue(result.hasErrors());
    }

    private Utlatande buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsnamn,
            boolean createUnit) {
        return buildIntyg(intygsKod, patientEfternamn, patientFornamn, "19121212-1212", hosPersonalFullstandigtNamn, "hosHsaId", enhetsnamn, "enhetHsaId",
                createUnit);
    }

    private Utlatande buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String patientPersonId,
            String hosPersonalFullstandigtNamn, String hosPersonalHsaId, String enhetsnamn, String enhetHsaId,
            boolean createUnit) {
        Utlatande intyg = new Utlatande();
        TypAvUtlatande typAvIntyg = new TypAvUtlatande();
        typAvIntyg.setCode(intygsKod);
        intyg.setTypAvUtlatande(typAvIntyg);
        Patient patient = new Patient();
        patient.setEfternamn(patientEfternamn);
        if (patientFornamn != null) {
            patient.getFornamn().add(patientFornamn);
        }
        if (patientPersonId != null) {
            patient.setPersonId(new PersonId());
            patient.getPersonId().setExtension(patientPersonId);
        }
        intyg.setPatient(patient);
        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setFullstandigtNamn(hosPersonalFullstandigtNamn);
        if (hosPersonalHsaId != null) {
            hosPersonal.setPersonalId(new HsaId());
            hosPersonal.getPersonalId().setExtension(hosPersonalHsaId);
        }
        if (createUnit) {
            Enhet enhet = new Enhet();
            enhet.setEnhetsnamn(enhetsnamn);
            if (enhetHsaId != null) {
                enhet.setEnhetsId(new HsaId());
                enhet.getEnhetsId().setExtension(enhetHsaId);
            }
            hosPersonal.setEnhet(enhet);
        }
        intyg.setSkapadAv(hosPersonal);
        return intyg;
    }
}
