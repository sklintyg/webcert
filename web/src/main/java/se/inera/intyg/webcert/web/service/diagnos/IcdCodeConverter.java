/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@Component
public class IcdCodeConverter {

    private static final String CODE_HEADING = "Kod";
    private static final int DIAGNOSIS_CODE_INDEX = 0;
    private static final int DIAGNOSIS_TITLE_INDEX = 3;

    public Diagnos convert(String line) {
        return toDiagnosis(line);
    }

    private Diagnos toDiagnosis(String line) {
        final var text = line.replace("\"", "").split("\t");
        if (isDiagnosisChapter(text) || isDiagnosisGroup(text) || isNotActive(text) || isHeading(text)) {
            return null;
        }
        final var diagnos = new Diagnos();
        diagnos.setKod(text[DIAGNOSIS_CODE_INDEX].replace(".", ""));
        diagnos.setBeskrivning(text[DIAGNOSIS_TITLE_INDEX]);
        return diagnos;
    }

    private boolean isHeading(String[] text) {
        return text[0].equals(CODE_HEADING);
    }

    private boolean isNotActive(String[] line) {
        return line[1].isEmpty();
    }

    private boolean isDiagnosisGroup(String[] line) {
        return line[0].contains("-");
    }

    private boolean isDiagnosisChapter(String[] line) {
        return Character.isDigit(line[0].charAt(0));
    }
}
