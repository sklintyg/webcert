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

package se.inera.intyg.webcert.web.service.diagnos;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DiagnosServiceImplTest {

    @Test
    public void testValidateDiagnosisCodeIcd10() throws Exception {
        // Verifies that the comment "Tested with: A11, A11.1, A11.1X, A111, A111X, A1111"
        // for se.inera.intyg.webcert.web.service.diagnos.DiagnosServiceImpl.ICD10_CODE_REGEXP is true

        //Given
        final List<String> codes = Arrays.asList("A11", "A11.1", "A11.1X", "A111", "A111X", "A1111");

        for (String code : codes) {
            //When
            final boolean result = new DiagnosServiceImpl().validateDiagnosisCode(code, "ICD_10_SE");

            //Then
            assertTrue(result);
        }
    }

    @Test
    public void testValidateDiagnosisCodeKsh97p() throws Exception {
        // Verifies that the comment "Tested with: A11, A11-P, A11-, A111, A111P"
        // for se.inera.intyg.webcert.web.service.diagnos.DiagnosServiceImpl.KSH97P_CODE_REGEXP is true

        //Given
        final List<String> codes = Arrays.asList("A11", "A11-P", "A11-", "A111", "A111P");

        for (String code : codes) {
            //When
            final boolean result = new DiagnosServiceImpl().validateDiagnosisCode(code, "KSH_97_P");

            //Then
            assertTrue(result);
        }
    }

}
