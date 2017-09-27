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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest extends BaseCreateDraftCertificateValidatorImplTest {

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Test
    public void testValidate() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(FK7263)).thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientEfternamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, null, "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientFornamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", null, "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", null, "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId",
                        true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdExtensionMissing() {
        ResultValidator result = validator
                .validate(
                        buildIntyg(FK7263, "efternamn", "förnamn", "", "fullständigt namn", "hosHsaId", "enhetsnamn", "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn",
                        "enhetHsaId", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientSamordningsnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19800191-0002", "fullständigt namn", "hosHsaId", "enhetsnamn",
                        "enhetHsaId", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientInvalidPersonnummer() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19010101-0101", "fullständigt namn", "hosHsaId", "enhetsnamn",
                        "enhetHsaId", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "fornamn", null, "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHosPersonalPersonalIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", null, "enhetsnamn", "enhetHsaId",
                        true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHosPersonalPersonalIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "", "enhetsnamn", "enhetHsaId",
                        true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "fornamn", "fullständigt namn", "enhetsnamn", false));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetsnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "fornamn", "fullständigt namn", null, true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateEnhetEnhetsIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn", null,
                        true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateEnhetEnhetsIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "19121212-1212", "fullständigt namn", "hosHsaId", "enhetsnamn", "",
                        true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testPuServiceLooksUpPatientForTsBas() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientCouldNotBeLookedUpInPu() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientIsSekretessmarkerad() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testValidateIntygstypPrivilege() {
        // We do the same validation as to view the utkast when CreateDraftCertificate.

        ResultValidator result = validator
                .validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), buildUserUnauthorized());
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver, times(0)).getSekretessStatus(any(Personnummer.class));
    }

    private Utlatande buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsnamn,
            boolean createUnit) {
        return buildIntyg(intygsKod, patientEfternamn, patientFornamn, "19121212-1212", hosPersonalFullstandigtNamn, "hosHsaId", enhetsnamn,
                "enhetHsaId",
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
