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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest {

    private static final String CODE = "CODE";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private WebcertFeatureService featureService;

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Before
    public void setup() {
        when(featureService.isModuleFeatureActive(eq(WebcertFeature.HANTERA_INTYGSUTKAST.getName()), eq(CODE.toLowerCase())))
                .thenReturn(Boolean.TRUE);
        when(moduleRegistry.moduleExists(CODE.toLowerCase())).thenReturn(Boolean.TRUE);
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
    }

    @Test
    public void testValidate() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(CODE.toLowerCase())).thenReturn(false);
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
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, null));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonIdExtensionMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, ""));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientPersonnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "191212121212"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientSamordningsnummerOk() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "198001910002"));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidatePatientInvalidPersonnummer() {
        ResultValidator result = validator
                .validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true, "190101010101"));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", null, "enhetsnamn", true));
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
    public void testValidateFeatureNotActive() {
        when(featureService.isModuleFeatureActive(eq(WebcertFeature.HANTERA_INTYGSUTKAST.getName()), eq(CODE.toLowerCase())))
                .thenReturn(false);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
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
