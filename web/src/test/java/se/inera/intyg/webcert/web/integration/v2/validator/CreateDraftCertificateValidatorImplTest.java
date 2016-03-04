/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.integration.v2.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.TypAvIntyg;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateValidatorImplTest {

    private static final String CODE = "CODE";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private CreateDraftCertificateValidatorImpl validator;

    @Test
    public void testValidate() {
        when(moduleRegistry.moduleExists(CODE.toLowerCase())).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertFalse(result.hasErrors());
    }

    @Test
    public void testValidateInvalidIntygsTyp() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.FALSE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientEfternamnMissing() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, null, "förnamn", "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidatePatientFornamnMissing() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", null, "fullständigt namn", "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalFullstandigtnamnMissing() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", null, "enhetsnamn", true));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetMissing() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", "fullständigt namn", "enhetsnamn", false));
        assertTrue(result.hasErrors());
    }

    @Test
    public void testValidateHoSPersonalEnhetsnamnMissing() {
        when(moduleRegistry.moduleExists(CODE)).thenReturn(Boolean.TRUE);
        ResultValidator result = validator.validate(buildIntyg(CODE, "efternamn", "fornamn", "fullständigt namn", null, true));
        assertTrue(result.hasErrors());
    }

    private Intyg buildIntyg(String intygsKod, String patientEfternamn, String patientFornamn, String hosPersonalFullstandigtNamn, String enhetsnamn,
            boolean createUnit) {
        Intyg intyg = new Intyg();
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsKod);
        intyg.setTypAvIntyg(typAvIntyg);
        Patient patient = new Patient();
        patient.setEfternamn(patientEfternamn);
        patient.setFornamn(patientFornamn);
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
