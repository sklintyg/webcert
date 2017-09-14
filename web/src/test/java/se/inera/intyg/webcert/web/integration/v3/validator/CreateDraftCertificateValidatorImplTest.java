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
package se.inera.intyg.webcert.web.integration.v3.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest {

    private static final String FK7263 = "fk7263";
    private static final String TSBAS = "ts-bas";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private WebcertFeatureService featureService;

    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Before
    public void setup() {

        when(commonAuthoritiesResolver.getSekretessmarkeringAllowed())
                .thenReturn(Arrays.asList("fk7263", "lisjp", "luse", "luae_na", "luae_fs", "db", "doi"));

        when(featureService.isModuleFeatureActive(eq(WebcertFeature.HANTERA_INTYGSUTKAST.getName()), eq(FK7263.toLowerCase())))
                .thenReturn(Boolean.TRUE);
        when(featureService.isModuleFeatureActive(eq(WebcertFeature.HANTERA_INTYGSUTKAST.getName()), eq(TSBAS.toLowerCase())))
                .thenReturn(Boolean.TRUE);

        when(moduleRegistry.moduleExists(FK7263.toLowerCase())).thenReturn(Boolean.TRUE);
        when(moduleRegistry.moduleExists(TSBAS)).thenReturn(true);
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
    }

    @Test
    public void testValidate() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(FK7263.toLowerCase())).thenReturn(false);
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
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, null));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdExtensionMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, ""));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "191212121212"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientSamordningsnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "198001910002"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientInvalidPersonnummer() {
        ResultValidator result = validator
                .validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "190101010101"));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "fornamn", null, "enhetsnamn", true));
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
    public void testValidateFeatureNotActive() {
        when(featureService.isModuleFeatureActive(eq(WebcertFeature.HANTERA_INTYGSUTKAST.getName()), eq(FK7263.toLowerCase())))
                .thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(FK7263, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testPuServiceLooksUpPatientForTsBas() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientCouldNotBeLookedUpInPu() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    @Test
    public void testTsBasIsNotAllowedWhenPatientIsSekretessmarkerad() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        ResultValidator result = validator.validateApplicationErrors(buildIntyg(TSBAS, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));
    }

    private Intyg buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsnamn,
            boolean createUnit) {
        return buildIntyg(intygsKod, patientEfternamn, patientFornamn, hosPersonalFullstandigtNamn, enhetsnamn, createUnit, "191212121212");
    }

    private Intyg buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn,
            String enhetsnamn,
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
        if (createUnit) {
            Enhet enhet = new Enhet();
            enhet.setEnhetsnamn(enhetsnamn);
            hosPersonal.setEnhet(enhet);
        }
        intyg.setSkapadAv(hosPersonal);
        return intyg;
    }
}
