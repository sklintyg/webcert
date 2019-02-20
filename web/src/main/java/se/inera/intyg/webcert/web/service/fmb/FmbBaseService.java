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
package se.inera.intyg.webcert.web.service.fmb;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;

public abstract class FmbBaseService {

    protected DiagnosInformationRepository repository;

    protected FmbBaseService(final DiagnosInformationRepository repository) {
        this.repository = repository;
    }

    protected Tuple2<String, Optional<DiagnosInformation>> searchDiagnosInformationByIcd10Kod(final String icd10Kod) {

        final int minCharCount = 3;

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
