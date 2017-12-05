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
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    public void testValidatePersonnummerDoesNotExistInPU() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.NOT_FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());

        verify(patientDetailsResolver).getPersonFromPUService(any(Personnummer.class));
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
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientCouldNotBeLookedUpInPu() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientIsSekretessmarkerad() {
        when(commonAuthoritiesResolver.getSekretessmarkeringAllowed())
                .thenReturn(Arrays.asList(Fk7263EntryPoint.MODULE_ID));
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }
    @Test
    public void testCreateDBAllowedWhenPatientIsDeceased() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.isAvliden(any(Personnummer.class))).thenReturn(true);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(DbModuleEntryPoint.MODULE_ID, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), buildUserUnauthorized());
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).isAvliden(any(Personnummer.class));
    }

    @Test
    public void testCreateDOIAllowedWhenPatientIsDeceased() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.isAvliden(any(Personnummer.class))).thenReturn(true);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(DoiModuleEntryPoint.MODULE_ID, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), buildUserUnauthorized());
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).isAvliden(any(Personnummer.class));
    }

    @Test
    public void testCreateOtherTypesNotAllowedWhenPatientIsDeceased() {
        List<String> typesToTest = Arrays.asList(Fk7263EntryPoint.MODULE_ID,
                TsBasEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID,
                LisjpEntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID,
                LuaenaEntryPoint.MODULE_ID);

        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.isAvliden(any(Personnummer.class))).thenReturn(true);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
       for(String type: typesToTest) {
           ResultValidator result = validator.validateApplicationErrors(buildIntyg(type, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true), user);
           assertTrue(result.hasErrors());
       }

        verify(patientDetailsResolver, times(typesToTest.size())).isAvliden(any(Personnummer.class));
    }

    private PersonSvar buildPersonSvar(PersonSvar.Status status) {
        Personnummer personnummer = new Personnummer("19121212-1212");
        return new PersonSvar(new PersonSvar(
                new Person(personnummer, false, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), status));
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
