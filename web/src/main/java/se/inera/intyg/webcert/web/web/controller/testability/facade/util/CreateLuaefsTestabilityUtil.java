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
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOSES_LIST_ITEM_2_ID;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOSES_LIST_ITEM_3_ID;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANHORIGS_BESKRIVNING_SVAR_JSON_ID_1;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANNAT_SVAR_JSON_ID_1;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_JOURNALUPPGIFTER_SVAR_JSON_ID_1;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_UNDERSOKNING_AV_PATIENT_SVAR_JSON_ID_1;
import static se.inera.intyg.common.luae_fs.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_DEBUT_SVAR_ID_15;
import static se.inera.intyg.common.luae_fs.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_DEBUT_SVAR_JSON_ID_15;
import static se.inera.intyg.common.luae_fs.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_ID_16;
import static se.inera.intyg.common.luae_fs.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_JSON_ID_16;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_ID_26;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID_26;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.DIAGNOS_SVAR_ID_6;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANNANBESKRIVNING_DELSVAR_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_BESKRIVNING_DELSVAR_JSON_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KANNEDOM_SVAR_ID_2;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KANNEDOM_SVAR_JSON_ID_2;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KONTAKT_ONSKAS_SVAR_ID_26;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KONTAKT_ONSKAS_SVAR_JSON_ID_26;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_DELSVAR_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.OVRIGT_SVAR_ID_25;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.OVRIGT_SVAR_JSON_ID_25;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_ID_3;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_JSON_ID_3;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAG_SVAR_ID_4;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAG_SVAR_JSON_ID_4;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueBoolean;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDate;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDateListMaximal;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDateListMinimal;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueMaximalDiagnosisListFk;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueMinimalDiagnosisListFk;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueText;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueMedicalInvestigation;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueMedicalInvestigationList;

@Component
public class CreateLuaefsTestabilityUtil {

    public Map<String, CertificateDataValue> createMinimumValuesLuaefs() {
        final var values = new HashMap<String, CertificateDataValue>();
        final var underlagBaseratPa = getDataValueDateListMinimal(GRUNDFORMEDICINSKTUNDERLAG_UNDERSOKNING_AV_PATIENT_SVAR_JSON_ID_1,
            LocalDate.now());
        values.put(GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1, underlagBaseratPa);

        final var kannedomOmPatienten = getDataValueDate(KANNEDOM_SVAR_JSON_ID_2, LocalDate.now());
        values.put(KANNEDOM_SVAR_ID_2, kannedomOmPatienten);

        final var underlagFinns = getDataValueBoolean(UNDERLAGFINNS_SVAR_JSON_ID_3, false);
        values.put(UNDERLAGFINNS_SVAR_ID_3, underlagFinns);

        final var diagnos = getDataValueMinimalDiagnosisListFk(DIAGNOSES_LIST_ITEM_1_ID,
            Diagnos.create("A00", "ICD_10_SE", "Kolera", "Kolera"));
        values.put(DIAGNOS_SVAR_ID_6, diagnos);

        final var funktionsnedsattningDebut = getDataValueText(FUNKTIONSNEDSATTNING_DEBUT_SVAR_JSON_ID_15,
            "funktionsnedsattning debut");
        values.put(FUNKTIONSNEDSATTNING_DEBUT_SVAR_ID_15, funktionsnedsattningDebut);

        final var funktionsnedsattningPaverkan = getDataValueText(FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_JSON_ID_16,
            "funktionsnedsattning påverkan");
        values.put(FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_ID_16, funktionsnedsattningPaverkan);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesLuaefs() {
        final var values = new HashMap<String, CertificateDataValue>();
        final var underlagBaseratPa = getDataValueDateListMaximal(List.of(
                GRUNDFORMEDICINSKTUNDERLAG_JOURNALUPPGIFTER_SVAR_JSON_ID_1, GRUNDFORMEDICINSKTUNDERLAG_ANHORIGS_BESKRIVNING_SVAR_JSON_ID_1,
                GRUNDFORMEDICINSKTUNDERLAG_ANNAT_SVAR_JSON_ID_1
            ),
            LocalDate.now(), 3);
        values.put(GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1, underlagBaseratPa);

        final var annat = getDataValueText(GRUNDFORMEDICINSKTUNDERLAG_BESKRIVNING_DELSVAR_JSON_ID_1, "annat");
        values.put(GRUNDFORMEDICINSKTUNDERLAG_ANNANBESKRIVNING_DELSVAR_ID_1, annat);

        final var motivering = getDataValueText(MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_ID_1, "motivering");
        values.put(MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_DELSVAR_ID_1, motivering);

        final var kannedomOmPatienten = getDataValueDate(KANNEDOM_SVAR_JSON_ID_2, LocalDate.now());
        values.put(KANNEDOM_SVAR_ID_2, kannedomOmPatienten);

        final var underlagFinns = getDataValueBoolean(UNDERLAGFINNS_SVAR_JSON_ID_3, true);
        values.put(UNDERLAGFINNS_SVAR_ID_3, underlagFinns);

        final var underlag = CertificateDataValueMedicalInvestigationList.builder()
            .list(
                List.of(
                    getValueMedicalInvestigation(0, "NEUROPSYKIATRISKT", "neuropsykiatriskt"),
                    getValueMedicalInvestigation(1, "ARBETSTERAPEUT", "arbetsterapeut"),
                    getValueMedicalInvestigation(2, "LOGOPED", "logoped")
                )
            )
            .build();
        values.put(UNDERLAG_SVAR_ID_4, underlag);

        final var diagnos = getDataValueMaximalDiagnosisListFk(
            List.of(
                DIAGNOSES_LIST_ITEM_1_ID,
                DIAGNOSES_LIST_ITEM_2_ID,
                DIAGNOSES_LIST_ITEM_3_ID
            ),
            List.of(
                Diagnos.create("A00", "ICD_10_SE", "Kolera", "Kolera"),
                Diagnos.create("A20", "ICD_10_SE", "Pest", "Pest"),
                Diagnos.create("A04", "ICD_10_SE", "Andra bakteriella tarminfektioner", "Andra bakteriella tarminfektioner")
            )
        );
        values.put(DIAGNOS_SVAR_ID_6, diagnos);

        final var funktionsnedsattningDebut = getDataValueText(FUNKTIONSNEDSATTNING_DEBUT_SVAR_JSON_ID_15,
            "funktionsnedsattning debut");
        values.put(FUNKTIONSNEDSATTNING_DEBUT_SVAR_ID_15, funktionsnedsattningDebut);

        final var funktionsnedsattningPaverkan = getDataValueText(FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_JSON_ID_16,
            "funktionsnedsattning påverkan");
        values.put(FUNKTIONSNEDSATTNING_PAVERKAN_SVAR_ID_16, funktionsnedsattningPaverkan);

        final var ovrigt = getDataValueText(OVRIGT_SVAR_JSON_ID_25, "övrigt");
        values.put(OVRIGT_SVAR_ID_25, ovrigt);

        final var kontaktOnskas = getDataValueBoolean(KONTAKT_ONSKAS_SVAR_JSON_ID_26, true);
        values.put(KONTAKT_ONSKAS_SVAR_ID_26, kontaktOnskas);

        final var anledningTillKontakt = getDataValueText(ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID_26, "anledning till kontakt");
        values.put(ANLEDNING_TILL_KONTAKT_DELSVAR_ID_26, anledningTillKontakt);

        return values;
    }

    private static CertificateDataValueMedicalInvestigation getValueMedicalInvestigation(int id, String code, String text) {
        return CertificateDataValueMedicalInvestigation.builder()
            .date(
                CertificateDataValueDate.builder()
                    .id(UNDERLAG_SVAR_JSON_ID_4 + "[" + id + "].datum")
                    .date(LocalDate.now()).build()
            )
            .investigationType(
                CertificateDataValueCode.builder()
                    .id(UNDERLAG_SVAR_JSON_ID_4 + "[" + id + "].typ")
                    .code(code)
                    .build()
            )
            .informationSource(
                CertificateDataTextValue.builder()
                    .id(UNDERLAG_SVAR_JSON_ID_4 + "[" + id + "].hamtasFran")
                    .text(text)
                    .build()
            )
            .build();
    }
}
