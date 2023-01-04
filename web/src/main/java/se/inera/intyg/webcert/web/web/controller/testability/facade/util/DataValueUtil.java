/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOSES_LIST_ITEM_1_ID;

import java.time.LocalDate;
import java.util.List;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataUncertainDateValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;

public final class DataValueUtil {

    private DataValueUtil() {
    }

    public static CertificateDataUncertainDateValue getDataUncertainDateValue(String id, String value) {
        return CertificateDataUncertainDateValue.builder()
            .id(id)
            .value(value)
            .build();
    }

    public static CertificateDataValueBoolean getDataValueBoolean(String id, boolean selected) {
        return CertificateDataValueBoolean.builder()
            .id(id)
            .selected(selected)
            .build();
    }

    public static CertificateDataValueCode getDataValueCode(String id, String code) {
        return CertificateDataValueCode.builder()
            .id(id)
            .code(code)
            .build();
    }

    public static CertificateDataValueDate getDataValueDate(String id, LocalDate date) {
        return CertificateDataValueDate.builder()
            .id(id)
            .date(date)
            .build();
    }

    public static CertificateDataTextValue getDataValueText(String id, String text) {
        return CertificateDataTextValue.builder()
            .id(id)
            .text(text)
            .build();
    }

    public static CertificateDataValueCodeList getDataValueCodeList(String id, String code) {
        return CertificateDataValueCodeList.builder()
            .list(List.of(
                    getDataValueCode(id, code)
                )
            )
            .build();
    }

    public static CertificateDataValueDiagnosisList getDataValueMinimalDiagnosisListFk(String id, Diagnos diagnos) {
        final var certificateDataValueDiagnosis = CertificateDataValueDiagnosis.builder()
            .id(DIAGNOSES_LIST_ITEM_1_ID)
            .code(diagnos.getDiagnosKod())
            .terminology(diagnos.getDiagnosKodSystem())
            .description(diagnos.getDiagnosBeskrivning())
            .build();
        final var certificateDataValueDiagnosisList = CertificateDataValueDiagnosisList.builder()
            .list(
                List.of(
                    certificateDataValueDiagnosis
                )
            ).
            build();
        return certificateDataValueDiagnosisList;
    }
}
