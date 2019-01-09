/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@RunWith(MockitoJUnitRunner.class)
public class WebcertModuleServiceImplTest {

    @Mock
    private DiagnosService diagnosService;

    @InjectMocks
    private WebcertModuleServiceImpl service;

    @Test
    public void testValidateDiagnosisCode() {
        final String codeFragment = "code";
        final String codeSystem = "codesystem";
        when(diagnosService.getDiagnosisByCode(codeFragment, codeSystem)).thenReturn(DiagnosResponse.ok(new ArrayList<>(), true));
        boolean res = service.validateDiagnosisCode(codeFragment, codeSystem);

        assertTrue(res);
    }

    @Test
    public void testValidateDiagnosisCodeFalse() {
        final String codeFragment = "code";
        final String codeSystem = "codesystem";
        when(diagnosService.getDiagnosisByCode(codeFragment, codeSystem)).thenReturn(DiagnosResponse.notFound());
        boolean res = service.validateDiagnosisCode(codeFragment, codeSystem);

        assertFalse(res);
    }

    @Test
    public void testValidateDiagnosisCodeDiagnoskodverk() {
        final String codeFragment = "code";
        final Diagnoskodverk codeSystem = Diagnoskodverk.ICD_10_SE;
        when(diagnosService.getDiagnosisByCode(codeFragment, codeSystem)).thenReturn(DiagnosResponse.ok(new ArrayList<>(), true));
        boolean res = service.validateDiagnosisCode(codeFragment, codeSystem);

        assertTrue(res);
    }

    @Test
    public void testValidateDiagnosisCodeDiagnoskodverkFalse() {
        final String codeFragment = "code";
        final Diagnoskodverk codeSystem = Diagnoskodverk.ICD_10_SE;
        when(diagnosService.getDiagnosisByCode(codeFragment, codeSystem)).thenReturn(DiagnosResponse.notFound());
        boolean res = service.validateDiagnosisCode(codeFragment, codeSystem);

        assertFalse(res);
    }

    @Test
    public void testGetDescriptionFromDiagnosKodNull() {
        final String code = "code";
        final String codeSystemStr = "codesystem";
        when(diagnosService.getDiagnosisByCode(code, codeSystemStr)).thenReturn(DiagnosResponse.notFound());
        String res = service.getDescriptionFromDiagnosKod(code, codeSystemStr);

        assertEquals("", res);
    }

    @Test
    public void testGetDescriptionFromDiagnosKod() {
        final String code = "code";
        final String codeSystemStr = "codesystem";
        final String description = "description";
        Diagnos diagnos = new Diagnos();
        diagnos.setBeskrivning(description);

        when(diagnosService.getDiagnosisByCode(code, codeSystemStr)).thenReturn(DiagnosResponse.ok(Arrays.asList(diagnos), false));
        String res = service.getDescriptionFromDiagnosKod(code, codeSystemStr);

        assertEquals(description, res);
    }

    @Test
    public void testGetDescriptionFromDiagnosKodMoreThanOneResult() {
        final String code = "code";
        final String codeSystemStr = "codesystem";

        when(diagnosService.getDiagnosisByCode(code, codeSystemStr)).thenReturn(DiagnosResponse.ok(Arrays.asList(new Diagnos(), new Diagnos()), false));
        String res = service.getDescriptionFromDiagnosKod(code, codeSystemStr);

        assertEquals("", res);
    }
}
