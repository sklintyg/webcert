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
package se.inera.intyg.webcert.web.service.fmb;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;

public abstract class FmbBaseService {

    public static final int MINIMUM_NUMBER_OF_CODE_CHARACTERS = 3;

    protected DiagnosInformationRepository repository;

    protected FmbBaseService(final DiagnosInformationRepository repository) {
        this.repository = repository;
    }

    protected Tuple2<String, Optional<DiagnosInformation>> searchDiagnosInformationByIcd10Kod(final String icd10Kod) {

        final int minCharCount = MINIMUM_NUMBER_OF_CODE_CHARACTERS;

        String choppedCode = icd10Kod;
        Optional<DiagnosInformation> diagnosInformation = Optional.empty();
        while (choppedCode.length() >= minCharCount) {
            diagnosInformation = repository.findFirstByIcd10KodList_kod(choppedCode);

            if (diagnosInformation.isPresent()) {
                break;
            } else if (choppedCode.length() > minCharCount) {
                // Make the icd10-code one position shorter, and thus more general.
                choppedCode = StringUtils.chop(choppedCode);
            } else {
                break;
            }
        }

        return Tuple.of(choppedCode, diagnosInformation);
    }

}
