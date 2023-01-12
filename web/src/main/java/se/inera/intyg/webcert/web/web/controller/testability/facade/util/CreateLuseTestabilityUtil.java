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
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.AKTIVITETSBEGRANSNING_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.AKTIVITETSBEGRANSNING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.AVSLUTADBEHANDLING_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.AVSLUTADBEHANDLING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_FOR_NY_BEDOMNING_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_FOR_NY_BEDOMNING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_NY_BEDOMNING_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_NY_BEDOMNING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOSGRUND_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.DIAGNOS_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FORMAGATROTSBEGRANSNING_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FORMAGATROTSBEGRANSNING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_ANNAN_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_ANNAN_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_BALANSKOORDINATION_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_BALANSKOORDINATION_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_KOMMUNIKATION_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_KOMMUNIKATION_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_KONCENTRATION_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_KONCENTRATION_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_PSYKISK_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_PSYKISK_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_SYNHORSELTAL_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_SYNHORSELTAL_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANNANBESKRIVNING_DELSVAR_ID_1;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_BESKRIVNING_DELSVAR_JSON_ID_1;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.KANNEDOM_SVAR_ID_2;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.KANNEDOM_SVAR_JSON_ID_2;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.KONTAKT_ONSKAS_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.KONTAKT_ONSKAS_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_DELSVAR_ID_1;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_ID_1;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.OVRIGT_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.OVRIGT_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.PAGAENDEBEHANDLING_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.PAGAENDEBEHANDLING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.PLANERADBEHANDLING_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.PLANERADBEHANDLING_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.SJUKDOMSFORLOPP_SVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.SJUKDOMSFORLOPP_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.SUBSTANSINTAG_DELSVAR_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.SUBSTANSINTAG_SVAR_JSON_ID;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_ID_3;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_JSON_ID_3;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.UNDERLAG_SVAR_ID_4;
import static se.inera.intyg.common.luse.v1.model.converter.RespConstants.UNDERLAG_SVAR_JSON_ID_4;
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
import se.inera.intyg.common.luae_na.v1.model.converter.RespConstants;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueMedicalInvestigation;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueMedicalInvestigationList;

@Component
public class CreateLuseTestabilityUtil {

    public Map<String, CertificateDataValue> createMinimumValuesLuse() {
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
        values.put(DIAGNOS_SVAR_ID, diagnos);

        final var diagnosBakgrund = getDataValueText(DIAGNOSGRUND_SVAR_JSON_ID, "diagnosBakgrund");
        values.put(DIAGNOSGRUND_SVAR_ID, diagnosBakgrund);

        final var nyBedomningDiagnosgrund = getDataValueBoolean(DIAGNOSGRUND_NY_BEDOMNING_SVAR_JSON_ID, false);
        values.put(DIAGNOSGRUND_NY_BEDOMNING_SVAR_ID, nyBedomningDiagnosgrund);

        final var bakgrund = getDataValueText(SJUKDOMSFORLOPP_SVAR_JSON_ID, "bakgrund");
        values.put(SJUKDOMSFORLOPP_SVAR_ID, bakgrund);

        final var funktionsnedsattningIntellektuell = getDataValueText(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_JSON_ID,
            "funktionsnedsattning intellektuell");
        values.put(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_ID, funktionsnedsattningIntellektuell);

        final var aktivitetsbegransning = getDataValueText(AKTIVITETSBEGRANSNING_SVAR_JSON_ID, "aktivitetsbegransning");
        values.put(AKTIVITETSBEGRANSNING_DELSVAR_ID, aktivitetsbegransning);

        final var medicinskaForutsattningarForArbete = getDataValueText(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_JSON_ID,
            "medicinska förutsättningar för arbete");
        values.put(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_ID, medicinskaForutsattningarForArbete);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesLuse() {
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

        final var kannedomOmPatienten = getDataValueDate(RespConstants.KANNEDOM_SVAR_JSON_ID_2, LocalDate.now());
        values.put(RespConstants.KANNEDOM_SVAR_ID_2, kannedomOmPatienten);

        final var underlagFinns = getDataValueBoolean(RespConstants.UNDERLAGFINNS_SVAR_JSON_ID_3, true);
        values.put(RespConstants.UNDERLAGFINNS_SVAR_ID_3, underlagFinns);

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
        values.put(DIAGNOS_SVAR_ID, diagnos);

        final var diagnosBakgrund = getDataValueText(DIAGNOSGRUND_SVAR_JSON_ID, "diagnosBakgrund");
        values.put(DIAGNOSGRUND_SVAR_ID, diagnosBakgrund);

        final var nyBedomningDiagnosgrund = getDataValueBoolean(DIAGNOSGRUND_NY_BEDOMNING_SVAR_JSON_ID, true);
        values.put(DIAGNOSGRUND_NY_BEDOMNING_SVAR_ID, nyBedomningDiagnosgrund);

        final var vilkaDiagnoser = getDataValueText(DIAGNOSGRUND_FOR_NY_BEDOMNING_DELSVAR_JSON_ID,
            "Beskriv vilken eller vilka diagnoser som avses.");
        values.put(DIAGNOSGRUND_FOR_NY_BEDOMNING_DELSVAR_ID, vilkaDiagnoser);

        final var bakgrund = getDataValueText(SJUKDOMSFORLOPP_SVAR_JSON_ID, "bakgrund");
        values.put(SJUKDOMSFORLOPP_SVAR_ID, bakgrund);

        final var funktionsnedsattningIntellektuell = getDataValueText(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_JSON_ID,
            "funktionsnedsattning intellektuell");
        values.put(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_ID, funktionsnedsattningIntellektuell);

        final var funktionsnedsattningKommunikation = getDataValueText(FUNKTIONSNEDSATTNING_KOMMUNIKATION_SVAR_JSON_ID,
            "funktionsnedsattning kommunikation");
        values.put(FUNKTIONSNEDSATTNING_KOMMUNIKATION_SVAR_ID, funktionsnedsattningKommunikation);

        final var funktionsnedsattningKoncentration = getDataValueText(FUNKTIONSNEDSATTNING_KONCENTRATION_SVAR_JSON_ID,
            "funktionsnedsattning koncentration");
        values.put(FUNKTIONSNEDSATTNING_KONCENTRATION_SVAR_ID, funktionsnedsattningKoncentration);

        final var funktionsnedsattningPsykisk = getDataValueText(FUNKTIONSNEDSATTNING_PSYKISK_SVAR_JSON_ID,
            "funktionsnedsattning psykisk");
        values.put(FUNKTIONSNEDSATTNING_PSYKISK_SVAR_ID, funktionsnedsattningPsykisk);

        final var funktionsnedsattningSynHorselTal = getDataValueText(FUNKTIONSNEDSATTNING_SYNHORSELTAL_SVAR_JSON_ID,
            "funktionsnedsattning synHorselTal");
        values.put(FUNKTIONSNEDSATTNING_SYNHORSELTAL_SVAR_ID, funktionsnedsattningSynHorselTal);

        final var funktionsnedsattningbalansKoordination = getDataValueText(FUNKTIONSNEDSATTNING_BALANSKOORDINATION_SVAR_JSON_ID,
            "funktionsnedsattning synHorselTal");
        values.put(FUNKTIONSNEDSATTNING_BALANSKOORDINATION_SVAR_ID, funktionsnedsattningbalansKoordination);

        final var funktionsnedsattningAnnan = getDataValueText(FUNKTIONSNEDSATTNING_ANNAN_SVAR_JSON_ID,
            "funktionsnedsattning synHorselTal");
        values.put(FUNKTIONSNEDSATTNING_ANNAN_SVAR_ID, funktionsnedsattningAnnan);

        final var aktivitetsbegransning = getDataValueText(AKTIVITETSBEGRANSNING_SVAR_JSON_ID, "aktivitetsbegransning");
        values.put(AKTIVITETSBEGRANSNING_DELSVAR_ID, aktivitetsbegransning);

        final var avslutadBehandling = getDataValueText(AVSLUTADBEHANDLING_SVAR_JSON_ID, "avslutad behandling");
        values.put(AVSLUTADBEHANDLING_DELSVAR_ID, avslutadBehandling);

        final var pagaendeBehandling = getDataValueText(PAGAENDEBEHANDLING_SVAR_JSON_ID, "pågående behandling");
        values.put(PAGAENDEBEHANDLING_DELSVAR_ID, pagaendeBehandling);

        final var planeradBehandling = getDataValueText(PLANERADBEHANDLING_SVAR_JSON_ID, "planerad behandling");
        values.put(PLANERADBEHANDLING_DELSVAR_ID, planeradBehandling);

        final var substansintag = getDataValueText(SUBSTANSINTAG_SVAR_JSON_ID, "substansintag");
        values.put(SUBSTANSINTAG_DELSVAR_ID, substansintag);

        final var medicinskaForutsattningarForArbete = getDataValueText(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_JSON_ID,
            "medicinska förutsättningar för arbete");
        values.put(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_ID, medicinskaForutsattningarForArbete);

        final var trotsBegransningar = getDataValueText(FORMAGATROTSBEGRANSNING_SVAR_JSON_ID, "förmåga trots begränsningar");
        values.put(FORMAGATROTSBEGRANSNING_SVAR_ID, trotsBegransningar);

        final var ovrigt = getDataValueText(OVRIGT_SVAR_JSON_ID, "övrigt");
        values.put(OVRIGT_SVAR_ID, ovrigt);

        final var kontaktOnskas = getDataValueBoolean(KONTAKT_ONSKAS_SVAR_JSON_ID, true);
        values.put(KONTAKT_ONSKAS_SVAR_ID, kontaktOnskas);

        final var anledningTillKontakt = getDataValueText(ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID, "anledning till kontakt");
        values.put(ANLEDNING_TILL_KONTAKT_DELSVAR_ID, anledningTillKontakt);

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
