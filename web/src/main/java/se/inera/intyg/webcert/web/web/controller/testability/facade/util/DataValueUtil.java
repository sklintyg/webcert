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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataUncertainDateValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateList;
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

    public static CertificateDataValueCodeList getDataValueCodeListMaximumValues(List<String> id, List<String> code) {
        final var certificateDataValueCodes = new ArrayList<CertificateDataValueCode>();
        for (int i = 0; i < id.size(); i++) {
            certificateDataValueCodes.add(
                getDataValueCode(id.get(i), code.get(i))
            );
        }
        return CertificateDataValueCodeList.builder()
            .list(
                certificateDataValueCodes
            )
            .build();
    }

    public static CertificateDataValueDateList getDataValueDateListMinimal(String id, LocalDate date) {
        return CertificateDataValueDateList.builder()
            .list(List.of(
                    getDataValueDate(id, date)
                )
            )
            .build();
    }

    public static CertificateDataValueDateList getDataValueDateListMaximal(List<String> listOfIds, LocalDate date, int numberOfDates) {
        final var certificateDataValueDateList = new ArrayList<CertificateDataValueDate>();
        for (int i = 0; i < numberOfDates; i++) {
            certificateDataValueDateList.add(getDataValueDate(listOfIds.get(i), date));
        }
        return CertificateDataValueDateList.builder()
            .list(
                certificateDataValueDateList
            )
            .build();
    }

    public static CertificateDataValueDiagnosisList getDataValueMinimalDiagnosisListFk(String id, Diagnos diagnos) {
        final var certificateDataValueDiagnosis = CertificateDataValueDiagnosis.builder()
            .id(id)
            .code(diagnos.getDiagnosKod())
            .terminology(diagnos.getDiagnosKodSystem())
            .description(diagnos.getDiagnosBeskrivning())
            .build();
        return CertificateDataValueDiagnosisList.builder()
            .list(
                List.of(
                    certificateDataValueDiagnosis
                )
            )
            .build();
    }

    public static CertificateDataValueDiagnosisList getDataValueMaximalDiagnosisListFk(List<String> ids, List<Diagnos> diagnosis) {
        final var certificateDataValueDiagnoses = new ArrayList<CertificateDataValueDiagnosis>();
        for (int i = 0; i < diagnosis.size(); i++) {
            certificateDataValueDiagnoses.add(
                CertificateDataValueDiagnosis.builder()
                    .id(ids.get(i))
                    .code(diagnosis.get(i).getDiagnosKod())
                    .terminology(diagnosis.get(i).getDiagnosKodSystem())
                    .description(diagnosis.get(i).getDiagnosBeskrivning())
                    .build()
            );
        }
        return CertificateDataValueDiagnosisList.builder()
            .list(
                certificateDataValueDiagnoses
            )
            .build();
    }
}
