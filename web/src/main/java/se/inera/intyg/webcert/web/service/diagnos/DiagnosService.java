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

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;

/**
 * Supplies services related to diagnosises.
 *
 * @author npet
 *
 */
public interface DiagnosService {

    /**
     * Returns all diagnoses in the repository exactly matching the code.
     *
     * @param code
     *            The code to search for.
     * @param codeSystem
     *            A String representing the code system to which the code belongs.
     */
    DiagnosResponse getDiagnosisByCode(String code, String codeSystem);

    /**
     * Returns all diagnoses in the repository exactly matching the code.
     *
     * @param code
     *            The code to search for.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     */
    DiagnosResponse getDiagnosisByCode(String code, Diagnoskodverk codeSystem);

    /**
     * Searches the repository for codes beginning with the codeFragment. Limits the number of matches returned.
     *
     * @param codeFragment
     *            The string to search codes by. The string must at least correspond to the pattern 'A01'.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @param nbrOfResults
     *            The number of results to return, must be larger than 0.
     */
    DiagnosResponse searchDiagnosisByCode(String codeFragment, String codeSystem, int nbrOfResults);

    /**
     * Searches the repository for descriptions matching searchString. Limits the number of matches returned.
     *
     * @param searchString
     *            The string to search for in descriptions.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @param nbrOfResults
     *            The number of results to return, must be larger than 0.
     */
    DiagnosResponse searchDiagnosisByDescription(String searchString, String codeSystem, int nbrOfResults);

    /**
     * Validates that the supplied code fragment is syntactically correct.
     *
     * @param codeFragment
     *            The code to be validated.
     * @param codeSystem
     *            The code system to which the code belongs (i.e ICD-10-SE or KSH97P)
     * @return true if the code fragment is syntactically correct.
     */
    boolean validateDiagnosisCode(String codeFragment, String codeSystem);
}
