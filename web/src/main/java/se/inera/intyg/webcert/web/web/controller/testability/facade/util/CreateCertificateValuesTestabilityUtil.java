/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.value.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.*;

@Component
public class CreateCertificateValuesTestabilityUtil {
    private static final int DEFAULT_SICK_LEAVE_LENGTH = 14;
    private static final int DEFAULT_SHORT_SICK_LEAVE_LENGTH = 4;

    @Autowired
    public CreateCertificateValuesTestabilityUtil() {
    }

    public CertificateDataValueDateRangeList getCertificateDataValueDateRangeList(boolean shortPeriod) {
        final var bedomning = CertificateDataValueDateRangeList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueDateRange.builder()
                                        .id("HELT_NEDSATT")
                                        .from(LocalDate.now())
                                        .to(LocalDate.now().plusDays(shortPeriod ? DEFAULT_SHORT_SICK_LEAVE_LENGTH : DEFAULT_SICK_LEAVE_LENGTH))
                                        .build()
                        )
                )
                .build();
        return bedomning;
    }

    public CertificateDataValueDiagnosisList getCertificateDataValueDiagnosisList() {
        final CertificateDataValueDiagnosisList diagnos = CertificateDataValueDiagnosisList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueDiagnosis.builder()
                                        .id("1")
                                        .terminology("ICD_10_SE")
                                        .code("J09")
                                        .description("Influensa orsakad av identifierat zoonotiskt eller pandemiskt influensavirus")
                                        .build()
                        )
                )
                .build();
        return diagnos;
    }
}
