/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateValidatorTest;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest extends BaseCreateDraftCertificateValidatorTest {

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Test
    public void testDeprecatedDoesNotValidate() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidate() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(LUSE.toLowerCase())).thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientEfternamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, null, "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientFornamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", null,
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(LUSE, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true, null));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdExtensionMissing() {
        ResultValidator result = validator
                .validate(buildIntyg(LUSE, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true, ""));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(LUSE, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true, "191212121212"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientSamordningsnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(LUSE, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true, "198001910002"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientInvalidPersonnummer() {
        ResultValidator result = validator
                .validate(buildIntyg(LUSE, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true, "190101010101"));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "fornamn",
                null, "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "fornamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", false));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetsIdMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "fornamn",
                "fullständigt namn", null, "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetsnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "fornamn",
                "fullständigt namn", "enhetsId", null, true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateFeatureNotActive() {
        when(authoritiesHelper.isFeatureActive(eq(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST), eq(LUSE.toLowerCase())))
                .thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(LUSE, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidationOfPersonnummerDoesNotExistInPU() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class)))
                .thenReturn(buildPersonSvar(PersonSvar.Status.NOT_FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());

        verify(patientDetailsResolver).getPersonFromPUService(any(Personnummer.class));
    }

    @Test
    public void testPuServiceLooksUpPatientForTsBas() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        ResultValidator result = validator
                .validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientCouldNotBeLookedUpInPu() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class)))
                .thenReturn(buildPersonSvar(PersonSvar.Status.NOT_FOUND));
        ResultValidator result = validator
                .validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getPersonFromPUService(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientIsSekretessmarkerad() {
        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering())
                .thenReturn(new HashSet<>(Arrays.asList(Fk7263EntryPoint.MODULE_ID)));
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        ResultValidator result = validator
                .validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                        "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testValidateIntygstypPrivilege() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class))).thenReturn(buildPersonSvar(PersonSvar.Status.FOUND));

        // We do the same validation as to view the utkast when CreateDraftCertificate.
        ResultValidator result = validator
                .validateApplicationErrors(
                        buildIntyg(LisjpEntryPoint.MODULE_ID, "efternamn", "förnamn",
                                "fullständigt namn", "enhetsId", "enhetsnamn", true), user);

        assertTrue(result.hasErrors());
        verify(patientDetailsResolver, times(0)).getSekretessStatus(any(Personnummer.class));
    }

    private PersonSvar buildPersonSvar(PersonSvar.Status status) {
        Personnummer personnummer = new Personnummer("19121212-1212");
        return new PersonSvar(new PersonSvar(
                new Person(personnummer, false, false, "fnamn", "mnamn", "enamn", "paddr", "pnr", "port"), status));
    }

    private Intyg buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsId, String enhetsnamn, boolean createUnit) {
        return buildIntyg(intygsKod, patientEfternamn, patientFornamn, hosPersonalFullstandigtNamn, enhetsId, enhetsnamn, createUnit,
                "191212121212");
    }

    private Intyg buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsId, String enhetsnamn,
            boolean createUnit, String personId) {
        Intyg intyg = new Intyg();

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsKod);
        intyg.setTypAvIntyg(typAvIntyg);

        Patient patient = new Patient();
        patient.setEfternamn(patientEfternamn);
        patient.setFornamn(patientFornamn);
        if (personId != null) {
            patient.setPersonId(new PersonId());
            patient.getPersonId().setExtension(personId);
        }
        intyg.setPatient(patient);

        HosPersonal hosPersonal = new HosPersonal();
        hosPersonal.setFullstandigtNamn(hosPersonalFullstandigtNamn);
        HsaId personalHsaId = new HsaId();
        personalHsaId.setExtension("personal-1");
        hosPersonal.setPersonalId(personalHsaId);
        if (createUnit) {
            Enhet enhet = new Enhet();
            enhet.setEnhetsId(new HsaId());
            enhet.getEnhetsId().setExtension(enhetsId);
            enhet.setEnhetsnamn(enhetsnamn);
            hosPersonal.setEnhet(enhet);
        }
        intyg.setSkapadAv(hosPersonal);

        return intyg;
    }
}
