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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
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

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest extends BaseCreateDraftCertificateValidatorTest {

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
            .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
        when(moduleRegistry.moduleExists(Fk7263EntryPoint.MODULE_ID)).thenReturn(true);
        when(moduleRegistry.moduleExists(LuseEntryPoint.MODULE_ID)).thenReturn(true);

        when(moduleRegistry.getIntygModule(eq(Fk7263EntryPoint.MODULE_ID))).thenReturn(buildIntygModule(FK7263, true));
        when(moduleRegistry.getIntygModule(eq(LuseEntryPoint.MODULE_ID))).thenReturn(buildIntygModule(LUSE, false));

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(new HashSet<>(ALL_INTYG_TYPES));

        user = buildUser();
    }

    @Test
    public void testDeprecatedDoesNotValidate() {
        ResultValidator result = validator.validateCertificateErrors(buildIntyg(FK7263, "efternamn", "förnamn",
            "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
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
        ResultValidator result = validator.validateCertificateErrors(buildIntyg(LUSE, "efternamn", "förnamn",
            "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
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
    public void testValidationOfPersonnummerDoesNotExistInPU() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class)))
            .thenReturn(PersonSvar.notFound());

        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
            "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());

        verify(patientDetailsResolver).getPersonFromPUService(any(Personnummer.class));
    }

    @Test
    public void testPuServiceLooksUpPatientForTsBas() throws ModuleNotFoundException {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(moduleRegistry.getIntygModule(eq(TsBasEntryPoint.MODULE_ID))).thenReturn(buildIntygModule(TSBAS, false));
        when(moduleRegistry.moduleExists(eq(TsBasEntryPoint.MODULE_ID))).thenReturn(true);

        ResultValidator result = validator
            .validateCertificateErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientCouldNotBeLookedUpInPu() throws ModuleNotFoundException {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class)))
            .thenReturn(PersonSvar.notFound());
        ResultValidator result = validator
            .validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn",
                "fullständigt namn", "enhetsId", "enhetsnamn", true), user);
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getPersonFromPUService(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientIsSekretessmarkerad() throws ModuleNotFoundException {
        final var mockEntryPoint = mock(DoiModuleEntryPoint.class);
        final var certificate = buildIntyg(TSBAS, "efternamn", "förnamn",
            "fullständigt namn", "enhetsId", "enhetsnamn", true);

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering())
            .thenReturn(new HashSet<>(Collections.singletonList(Fk7263EntryPoint.MODULE_ID)));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(moduleRegistry.getModuleEntryPoint(TSBAS)).thenReturn(mockEntryPoint);
        when(mockEntryPoint.getModuleName()).thenReturn(TsBasEntryPoint.MODULE_NAME);
        when(moduleRegistry.getIntygModule(eq(TsBasEntryPoint.MODULE_ID))).thenReturn(buildIntygModule(TSBAS, false));
        when(moduleRegistry.moduleExists(eq(TsBasEntryPoint.MODULE_ID))).thenReturn(true);

        ResultValidator result = validator.validateCertificateErrors(certificate, user);

        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testValidateIntygstypPrivilege() {
        // We do the same validation as to view the utkast when CreateDraftCertificate.
        ResultValidator result = validator
            .validateCertificateErrors(
                buildIntyg(LUSE, "efternamn", "förnamn",
                    "fullständigt namn", "enhetsId", "enhetsnamn", true), user);

        assertTrue(result.hasErrors());
        verify(patientDetailsResolver, times(0)).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void shouldIncludeModuleNameInErrorMessageForSekretessmarkerad() throws ModuleNotFoundException {
        final var user = buildUserUnauthorized();
        final var certificateType = LuseEntryPoint.MODULE_ID;
        final var certificateDisplayName = LuseEntryPoint.MODULE_NAME;
        final var certificate = buildIntyg(certificateType, "lastName", "firstName",
            "fullName", "unitId", "unitName", true);
        final var mockEntryPoint = mock(LuseEntryPoint.class);

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(new HashSet<>());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(moduleRegistry.getModuleEntryPoint(certificateType)).thenReturn(mockEntryPoint);
        when(mockEntryPoint.getModuleName()).thenReturn(certificateDisplayName);

        final var response = validator.validateCertificateErrors(certificate, user);

        assertEquals(1, response.getErrorMessages().size());
        assertTrue(response.getErrorMessages().get(0).contains(certificateDisplayName));
    }

    @Test
    public void shouldUseModuleIdInSekretessErrorMessageWhenFailureGettingName() throws ModuleNotFoundException {
        final var user = buildUserUnauthorized();
        final var certificateType = LuseEntryPoint.MODULE_ID;
        final var certificate = buildIntyg(certificateType, "lastName", "firstName",
            "fullName", "unitId", "unitName", true);

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(new HashSet<>());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(moduleRegistry.getModuleEntryPoint(certificateType)).thenThrow(ModuleNotFoundException.class);

        final var response = validator.validateCertificateErrors(certificate, user);

        assertEquals(1, response.getErrorMessages().size());
        assertTrue(response.getErrorMessages().get(0).contains(certificateType));
    }

    @Test
    public void shouldNotAddSecondErrorMessageWhenVardadminAndCertificateNotAllowedForSekretessmarkerad() throws ModuleNotFoundException {
        final var user = buildUserUnauthorized();
        user.getAuthorities().remove(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT);
        final var certificateType = LuseEntryPoint.MODULE_ID;
        final var certificateDisplayName = LuseEntryPoint.MODULE_NAME;
        final var certificate = buildIntyg(certificateType, "lastName", "firstName",
            "fullName", "unitId", "unitName", true);
        final var mockEntryPoint = mock(LuseEntryPoint.class);

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(new HashSet<>());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(moduleRegistry.getModuleEntryPoint(certificateType)).thenReturn(mockEntryPoint);
        when(mockEntryPoint.getModuleName()).thenReturn(certificateDisplayName);

        final var response = validator.validateCertificateErrors(certificate, user);

        assertEquals(1, response.getErrorMessages().size());
    }

    @Test
    public void shouldReturnErrorWhenSekretessStatusUndefined() throws ModuleNotFoundException {
        final var user = buildUserUnauthorized();
        final var certificateDisplayName = LuseEntryPoint.MODULE_NAME;
        final var certificate = buildIntyg(LUSE, "lastName", "firstName",
            "fullName", "unitId", "unitName", true);
        final var mockEntryPoint = mock(LuseEntryPoint.class);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        when(moduleRegistry.getModuleEntryPoint(LUSE)).thenReturn(mockEntryPoint);
        when(mockEntryPoint.getModuleName()).thenReturn(certificateDisplayName);

        final var response = validator.validateCertificateErrors(certificate, user);

        assertEquals(1, response.getErrorMessages().size());
    }

    @Test
    public void shouldReturnErrorWhenInvalidPersonIdIsProvided() {
        final var user = buildUser();
        final var certificate = buildIntyg(LUSE, "lastName", "firstName",
            "fullName", "unitId", "unitName", true);
        final var patient = certificate.getPatient();
        patient.setPersonId(new PersonId());
        patient.getPersonId().setExtension("invalidPersonId");
        certificate.setPatient(patient);

        final var response = validator.validateCertificateErrors(certificate, user);

        assertEquals(1, response.getErrorMessages().size());
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