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

import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BALANSRUBBNINGAR_YRSEL_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BALANSRUBBNINGAR_YRSEL_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BEHANDLING_DIABETES_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BEHORIGHET_LAKARE_SPECIALKOMPETENS_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BEHORIGHET_LAKARE_SPECIALKOMPETENS_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BINOKULART_MED_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.BINOKULART_UTAN_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.DUBBELSEENDE_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.DUBBELSEENDE_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_MEDVETANDESTORNING_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_MEDVETANDESTORNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HAR_DIABETES_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HAR_DIABETES_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HJART_ELLER_KARLSJUKDOM_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HJART_ELLER_KARLSJUKDOM_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HOGER_OGA_MED_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.HOGER_OGA_UTAN_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.IDENTITET_STYRKT_GENOM_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.INSULINBEHANDLING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.INTYG_AVSER_SVAR_ID_1;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.KONTAKTLINSER_HOGER_OGA_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.KONTAKTLINSER_VANSTER_OGA_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.KOSTBEHANDLING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.LAKEMEDEL_ORDINERAD_DOS_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.LAKEMEDEL_ORDINERAD_DOS_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.LAMPLIGHET_INNEHA_BEHORIGHET_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.MEDICINER_STADIGVARANDE_MEDICINERING_DELSVARSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.MEDICINER_STADIGVARANDE_MEDICINERING_DELSVARSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.MEDVETANDESTORNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.MEDVETANDESTORNING_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.NEDSATT_NJURFUNKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.NEDSATT_NJURFUNKTION_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.NYSTAGMUS_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.NYSTAGMUS_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.ORSAK_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.ORSAK_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.OTILLRACKLIG_RORELSEFORMAGA_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.OTILLRACKLIG_RORELSEFORMAGA_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.OVRIGA_KOMMENTARER_DELSVARSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.OVRIGA_KOMMENTARER_DELSVARSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PLATS_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PLATS_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PROGRESSIV_OGONSJUKDOM_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PROGRESSIV_OGONSJUKDOM_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PROVTAGNING_AVSEENDE_AKTUELLT_BRUK_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PROVTAGNING_AVSEENDE_AKTUELLT_BRUK_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PSYKISK_SJUKDOM_STORNING_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PSYKISK_SJUKDOM_STORNING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PSYKISK_UTVECKLINGSSTORNING_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.PSYKISK_UTVECKLINGSSTORNING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.RISKFAKTORER_STROKE_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.RISKFAKTORER_STROKE_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SEENDE_NEDSATT_BELYSNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SEENDE_NEDSATT_BELYSNING_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SJUKDOM_FUNKTIONSNEDSATTNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SJUKDOM_FUNKTIONSNEDSATTNING_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SYNFALTSDEFEKTER_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SYNFALTSDEFEKTER_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SYNKARPA_SKICKAS_SEPARAT_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.SYNKARPA_SKICKAS_SEPARAT_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TABLETTBEHANDLING_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_MISSBRUK_BEROENDE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_MISSBRUK_BEROENDE_JOURNAL_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_NEUROLOGISK_SJUKDOM_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_NEUROLOGISK_SJUKDOM_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_PA_HJARNSKADA_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_PA_HJARNSKADA_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_SOMN_ELLER_VAKENHETSSTORNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_SOMN_ELLER_VAKENHETSSTORNING_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TIDPUNKT_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TIDPUNKT_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TYP_AV_DIABETES_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TYP_AV_SJUKDOM_RISKFAKTORER_STROKE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TYP_AV_SJUKDOM_RISKFAKTORER_STROKE_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TYP_SJUKDOM_FUNKTIONSNEDSATTNING_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.TYP_SJUKDOM_FUNKTIONSNEDSATTNING_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.UNDERSOKNING_8_DIOPTRIERS_KORREKTIONSGRAD_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.UNDERSOKNING_8_DIOPTRIERS_KORREKTIONSGRAD_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.UPPFATTA_SAMTALSTAMMA_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.UPPFATTA_SAMTALSTAMMA_SVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.VANSTER_OGA_MED_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.VANSTER_OGA_UTAN_KORREKTION_JSON_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.VARDEN_FOR_SYNSKARPA_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.VARDINSATSER_MISSBRUK_BEROENDE_DELSVAR_ID;
import static se.inera.intyg.common.ts_bas.v7.codes.RespConstantsV7.VARDINSATSER_MISSBRUK_BEROENDE_JSON_ID;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueBoolean;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCode;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCodeList;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCodeListMaximumValues;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDouble;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueVisualAcuities;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueVisualAcuity;
import se.inera.intyg.common.ts_bas.v7.model.internal.BedomningKorkortstyp;
import se.inera.intyg.common.ts_bas.v7.model.internal.IntygAvserKategori;
import se.inera.intyg.common.ts_parent.codes.DiabetesKod;
import se.inera.intyg.common.ts_parent.codes.IdKontrollKod;

@Component
public class CreateTsBasTestabilityUtil {

    public Map<String, CertificateDataValue> createMinimumValuesTsBas() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var intygetAvser = getDataValueCodeList(IntygAvserKategori.IAV1.name(), IntygAvserKategori.IAV1.name());
        values.put(INTYG_AVSER_SVAR_ID_1, intygetAvser);

        final var identitetStyrktGenom = getDataValueCode(IdKontrollKod.ID_KORT.getCode(), IdKontrollKod.ID_KORT.getCode());
        values.put(IDENTITET_STYRKT_GENOM_SVAR_ID, identitetStyrktGenom);

        final var synfaltsDefekter = getDataValueBoolean(SYNFALTSDEFEKTER_JSON_ID, false);
        values.put(SYNFALTSDEFEKTER_SVAR_ID, synfaltsDefekter);

        final var nattblindhet = getDataValueBoolean(SEENDE_NEDSATT_BELYSNING_JSON_ID, false);
        values.put(SEENDE_NEDSATT_BELYSNING_SVAR_ID, nattblindhet);

        final var progressivOgonsjukdom = getDataValueBoolean(PROGRESSIV_OGONSJUKDOM_JSON_ID, false);
        values.put(PROGRESSIV_OGONSJUKDOM_SVAR_ID, progressivOgonsjukdom);

        final var dubbelseende = getDataValueBoolean(DUBBELSEENDE_JSON_ID, false);
        values.put(DUBBELSEENDE_SVAR_ID, dubbelseende);

        final var nystagmus = getDataValueBoolean(NYSTAGMUS_JSON_ID, false);
        values.put(NYSTAGMUS_SVAR_ID, nystagmus);

        final var synskarpaSkickasSeparat = getDataValueBoolean(SYNKARPA_SKICKAS_SEPARAT_JSON_ID, true);
        values.put(SYNKARPA_SKICKAS_SEPARAT_DELSVAR_ID, synskarpaSkickasSeparat);

        final var balansrubbning = getDataValueBoolean(BALANSRUBBNINGAR_YRSEL_JSON_ID, false);
        values.put(BALANSRUBBNINGAR_YRSEL_SVAR_ID, balansrubbning);

        final var funktionsnedsattning = getDataValueBoolean(SJUKDOM_FUNKTIONSNEDSATTNING_JSON_ID, false);
        values.put(SJUKDOM_FUNKTIONSNEDSATTNING_SVAR_ID, funktionsnedsattning);

        final var hjartOchKarl = getDataValueBoolean(HJART_ELLER_KARLSJUKDOM_JSON_ID, false);
        values.put(HJART_ELLER_KARLSJUKDOM_SVAR_ID, hjartOchKarl);

        final var hjarnskadaTrauma = getDataValueBoolean(TECKEN_PA_HJARNSKADA_JSON_ID, false);
        values.put(TECKEN_PA_HJARNSKADA_SVAR_ID, hjarnskadaTrauma);

        final var riskfaktorerStroke = getDataValueBoolean(RISKFAKTORER_STROKE_JSON_ID, false);
        values.put(RISKFAKTORER_STROKE_SVAR_ID, riskfaktorerStroke);

        final var diabetes = getDataValueBoolean(HAR_DIABETES_JSON_ID, false);
        values.put(HAR_DIABETES_SVAR_ID, diabetes);

        final var neurologiskSjukdom = getDataValueBoolean(TECKEN_NEUROLOGISK_SJUKDOM_JSON_ID, false);
        values.put(TECKEN_NEUROLOGISK_SJUKDOM_SVAR_ID, neurologiskSjukdom);

        final var medvetandestorning = getDataValueBoolean(MEDVETANDESTORNING_JSON_ID, false);
        values.put(MEDVETANDESTORNING_SVAR_ID, medvetandestorning);

        final var nedsattNjurfunktion = getDataValueBoolean(NEDSATT_NJURFUNKTION_JSON_ID, false);
        values.put(NEDSATT_NJURFUNKTION_SVAR_ID, nedsattNjurfunktion);

        final var kognitivFormoga = getDataValueBoolean(TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_JSON_ID, false);
        values.put(TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_SVAR_ID, kognitivFormoga);

        final var somnOchVakenhetsstorningar = getDataValueBoolean(TECKEN_SOMN_ELLER_VAKENHETSSTORNING_JSON_ID, false);
        values.put(TECKEN_SOMN_ELLER_VAKENHETSSTORNING_SVAR_ID, somnOchVakenhetsstorningar);

        final var alkoholJournal = getDataValueBoolean(TECKEN_MISSBRUK_BEROENDE_JOURNAL_JSON_ID, false);
        values.put(TECKEN_MISSBRUK_BEROENDE_DELSVAR_ID, alkoholJournal);

        final var alkoholVardinsats = getDataValueBoolean(VARDINSATSER_MISSBRUK_BEROENDE_JSON_ID, false);
        values.put(VARDINSATSER_MISSBRUK_BEROENDE_DELSVAR_ID, alkoholVardinsats);

        final var alhoholLakarordinerat = getDataValueBoolean(REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_JSON_ID, false);
        values.put(REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_DELSVAR_ID, alhoholLakarordinerat);

        final var psykiskStorning = getDataValueBoolean(PSYKISK_SJUKDOM_STORNING_DELSVAR_JSON_ID, false);
        values.put(PSYKISK_SJUKDOM_STORNING_DELSVAR_ID, psykiskStorning);

        final var utvecklingsStorning = getDataValueBoolean(PSYKISK_UTVECKLINGSSTORNING_DELSVAR_JSON_ID, false);
        values.put(PSYKISK_UTVECKLINGSSTORNING_DELSVAR_ID, utvecklingsStorning);

        final var symptom = getDataValueBoolean(ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_JSON_ID, false);
        values.put(ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_ID, symptom);

        final var sjukhusVard = getDataValueBoolean(FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID, false);
        values.put(FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID, sjukhusVard);

        final var stadigvarandeMedicinering = getDataValueBoolean(FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_JSON_ID, false);
        values.put(FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_ID, stadigvarandeMedicinering);

        final var bedomning = getDataValueCodeList(BedomningKorkortstyp.VAR11.name(), BedomningKorkortstyp.VAR11.name());
        values.put(LAMPLIGHET_INNEHA_BEHORIGHET_SVAR_ID, bedomning);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesTsBas() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var intygetAvser = getDataValueCodeListMaximumValuesIntygetAvser();
        values.put(INTYG_AVSER_SVAR_ID_1, intygetAvser);

        final var identitetStyrktGenom = getDataValueCode(IdKontrollKod.ID_KORT.getCode(), IdKontrollKod.ID_KORT.getCode());
        values.put(IDENTITET_STYRKT_GENOM_SVAR_ID, identitetStyrktGenom);

        final var synfaltsDefekter = getDataValueBoolean(SYNFALTSDEFEKTER_JSON_ID, true);
        values.put(SYNFALTSDEFEKTER_SVAR_ID, synfaltsDefekter);

        final var nattblindhet = getDataValueBoolean(SEENDE_NEDSATT_BELYSNING_JSON_ID, true);
        values.put(SEENDE_NEDSATT_BELYSNING_SVAR_ID, nattblindhet);

        final var progressivOgonsjukdom = getDataValueBoolean(PROGRESSIV_OGONSJUKDOM_JSON_ID, true);
        values.put(PROGRESSIV_OGONSJUKDOM_SVAR_ID, progressivOgonsjukdom);

        final var dubbelseende = getDataValueBoolean(DUBBELSEENDE_JSON_ID, true);
        values.put(DUBBELSEENDE_SVAR_ID, dubbelseende);

        final var nystagmus = getDataValueBoolean(NYSTAGMUS_JSON_ID, true);
        values.put(NYSTAGMUS_SVAR_ID, nystagmus);

        final var synskarpa = getCertificateDataValueVisualAcuities();
        values.put(VARDEN_FOR_SYNSKARPA_ID, synskarpa);

        final var korrektionsglasensStyrka = getDataValueBoolean(UNDERSOKNING_8_DIOPTRIERS_KORREKTIONSGRAD_JSON_ID, true);
        values.put(UNDERSOKNING_8_DIOPTRIERS_KORREKTIONSGRAD_SVAR_ID, korrektionsglasensStyrka);

        final var balansrubbning = getDataValueBoolean(BALANSRUBBNINGAR_YRSEL_JSON_ID, true);
        values.put(BALANSRUBBNINGAR_YRSEL_SVAR_ID, balansrubbning);

        final var uppfattaSamtalFyraMeter = getDataValueBoolean(UPPFATTA_SAMTALSTAMMA_JSON_ID, true);
        values.put(UPPFATTA_SAMTALSTAMMA_SVAR_ID, uppfattaSamtalFyraMeter);

        final var funktionsnedsattning = getDataValueBoolean(SJUKDOM_FUNKTIONSNEDSATTNING_JSON_ID, true);
        values.put(SJUKDOM_FUNKTIONSNEDSATTNING_SVAR_ID, funktionsnedsattning);

        final var funktionsnedsattningBeskrivning = getDataValueText(TYP_SJUKDOM_FUNKTIONSNEDSATTNING_JSON_ID, "beskrivning");
        values.put(TYP_SJUKDOM_FUNKTIONSNEDSATTNING_DELSVAR_ID, funktionsnedsattningBeskrivning);

        final var funktionsnedsattningRoresleformoga = getDataValueBoolean(OTILLRACKLIG_RORELSEFORMAGA_JSON_ID, false);
        values.put(OTILLRACKLIG_RORELSEFORMAGA_SVAR_ID, funktionsnedsattningRoresleformoga);

        final var hjartOchKarl = getDataValueBoolean(HJART_ELLER_KARLSJUKDOM_JSON_ID, true);
        values.put(HJART_ELLER_KARLSJUKDOM_SVAR_ID, hjartOchKarl);

        final var hjarnskadaTrauma = getDataValueBoolean(TECKEN_PA_HJARNSKADA_JSON_ID, true);
        values.put(TECKEN_PA_HJARNSKADA_SVAR_ID, hjarnskadaTrauma);

        final var riskfaktorerStroke = getDataValueBoolean(RISKFAKTORER_STROKE_JSON_ID, true);
        values.put(RISKFAKTORER_STROKE_SVAR_ID, riskfaktorerStroke);

        final var typAvSjukdom = getDataValueText(TYP_AV_SJUKDOM_RISKFAKTORER_STROKE_JSON_ID, "typ av sjukdom");
        values.put(TYP_AV_SJUKDOM_RISKFAKTORER_STROKE_DELSVAR_ID, typAvSjukdom);

        final var diabetes = getDataValueBoolean(HAR_DIABETES_JSON_ID, true);
        values.put(HAR_DIABETES_SVAR_ID, diabetes);

        final var diabetesKod = getDataValueCode(DiabetesKod.DIABETES_TYP_2.name(), DiabetesKod.DIABETES_TYP_2.name());
        values.put(TYP_AV_DIABETES_SVAR_ID, diabetesKod);

        final var diabetesBehandling = getDataValueCodeListMaximumValues(
            List.of(
                KOSTBEHANDLING_DELSVAR_JSON_ID, TABLETTBEHANDLING_DELSVAR_JSON_ID, INSULINBEHANDLING_DELSVAR_JSON_ID
            ),
            List.of(
                KOSTBEHANDLING_DELSVAR_JSON_ID, TABLETTBEHANDLING_DELSVAR_JSON_ID, INSULINBEHANDLING_DELSVAR_JSON_ID
            )
        );
        values.put(BEHANDLING_DIABETES_SVAR_ID, diabetesBehandling);

        final var neurologiskSjukdom = getDataValueBoolean(TECKEN_NEUROLOGISK_SJUKDOM_JSON_ID, true);
        values.put(TECKEN_NEUROLOGISK_SJUKDOM_SVAR_ID, neurologiskSjukdom);

        final var medvetandestorning = getDataValueBoolean(MEDVETANDESTORNING_JSON_ID, true);
        values.put(MEDVETANDESTORNING_SVAR_ID, medvetandestorning);

        final var medvetandestorningBeskrivning = getDataValueText(FOREKOMST_MEDVETANDESTORNING_JSON_ID,
            "ange när den inträffade och orsak");
        values.put(FOREKOMST_MEDVETANDESTORNING_DELSVAR_ID, medvetandestorningBeskrivning);

        final var nedsattNjurfunktion = getDataValueBoolean(NEDSATT_NJURFUNKTION_JSON_ID, true);
        values.put(NEDSATT_NJURFUNKTION_SVAR_ID, nedsattNjurfunktion);

        final var kognitivFormoga = getDataValueBoolean(TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_JSON_ID, true);
        values.put(TECKEN_SVIKTANDE_KOGNITIV_FUNKTION_SVAR_ID, kognitivFormoga);

        final var somnOchVakenhetsstorningar = getDataValueBoolean(TECKEN_SOMN_ELLER_VAKENHETSSTORNING_JSON_ID, true);
        values.put(TECKEN_SOMN_ELLER_VAKENHETSSTORNING_SVAR_ID, somnOchVakenhetsstorningar);

        final var alkoholJournal = getDataValueBoolean(TECKEN_MISSBRUK_BEROENDE_JOURNAL_JSON_ID, true);
        values.put(TECKEN_MISSBRUK_BEROENDE_DELSVAR_ID, alkoholJournal);

        final var alkoholVardinsats = getDataValueBoolean(VARDINSATSER_MISSBRUK_BEROENDE_JSON_ID, true);
        values.put(VARDINSATSER_MISSBRUK_BEROENDE_DELSVAR_ID, alkoholVardinsats);

        final var alkoholProvtagning = getDataValueBoolean(PROVTAGNING_AVSEENDE_AKTUELLT_BRUK_JSON_ID, true);
        values.put(PROVTAGNING_AVSEENDE_AKTUELLT_BRUK_DELSVAR_ID, alkoholProvtagning);

        final var alhoholLakarordinerat = getDataValueBoolean(REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_JSON_ID, true);
        values.put(REGELBUNDET_LAKARORDINERAT_BRUK_LAKEMEDEL_DELSVAR_ID, alhoholLakarordinerat);

        final var alkoholOrdineradDos = getDataValueText(LAKEMEDEL_ORDINERAD_DOS_DELSVAR_JSON_ID, "Läkemedel och ordinerad dos");
        values.put(LAKEMEDEL_ORDINERAD_DOS_DELSVAR_ID, alkoholOrdineradDos);

        final var psykiskStorning = getDataValueBoolean(PSYKISK_SJUKDOM_STORNING_DELSVAR_JSON_ID, true);
        values.put(PSYKISK_SJUKDOM_STORNING_DELSVAR_ID, psykiskStorning);

        final var utvecklingsStorning = getDataValueBoolean(PSYKISK_UTVECKLINGSSTORNING_DELSVAR_JSON_ID, true);
        values.put(PSYKISK_UTVECKLINGSSTORNING_DELSVAR_ID, utvecklingsStorning);

        final var symptom = getDataValueBoolean(ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_JSON_ID, true);
        values.put(ADHD_ADD_DAMP_ASPERGERS_TOURETTES_DELSVAR_ID, symptom);

        final var sjukhusVard = getDataValueBoolean(FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID, true);
        values.put(FOREKOMST_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID, sjukhusVard);

        final var sjukhusVardTidpunkt = getDataValueText(TIDPUNKT_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID, "tidpunkt");
        values.put(TIDPUNKT_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID, sjukhusVardTidpunkt);

        final var sjukhusVardVardinrattningensNamn = getDataValueText(PLATS_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID, "enhetens namn");
        values.put(PLATS_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID, sjukhusVardVardinrattningensNamn);

        final var sjukhusVardOrsak = getDataValueText(ORSAK_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_JSON_ID, "orsak");
        values.put(ORSAK_VARD_SJUKHUS_KONTAKT_LAKARE_DELSVAR_ID, sjukhusVardOrsak);

        final var stadigvarandeMedicinering = getDataValueBoolean(FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_JSON_ID, true);
        values.put(FOREKOMST_STADIGVARANDE_MEDICINERING_DELSVARSVAR_ID, stadigvarandeMedicinering);

        final var ovrigBeskrivning = getDataValueText(MEDICINER_STADIGVARANDE_MEDICINERING_DELSVARSVAR_JSON_ID,
            "Vilken eller vilka mediciner?");
        values.put(MEDICINER_STADIGVARANDE_MEDICINERING_DELSVARSVAR_ID, ovrigBeskrivning);

        final var ovrigKommentar = getDataValueText(OVRIGA_KOMMENTARER_DELSVARSVAR_JSON_ID,
            "Övriga kommentarer som är relevant ur trafiksäkerhetssynpunkt.");
        values.put(OVRIGA_KOMMENTARER_DELSVARSVAR_ID, ovrigKommentar);

        final var bedomning = getDataValueCodeListMaximumValuesBedomning();
        values.put(LAMPLIGHET_INNEHA_BEHORIGHET_SVAR_ID, bedomning);

        final var specialkompetens = getDataValueText(BEHORIGHET_LAKARE_SPECIALKOMPETENS_JSON_ID,
            "Patienten bör före ärendets avgörande undersökas av läkare med specialistkompetens i");
        values.put(BEHORIGHET_LAKARE_SPECIALKOMPETENS_SVAR_ID, specialkompetens);

        return values;
    }

    private static CertificateDataValueCodeList getDataValueCodeListMaximumValuesBedomning() {
        return getDataValueCodeListMaximumValues(
            List.of(
                BedomningKorkortstyp.VAR1.name(), BedomningKorkortstyp.VAR2.name(), BedomningKorkortstyp.VAR3.name(),
                BedomningKorkortstyp.VAR4.name(), BedomningKorkortstyp.VAR5.name(), BedomningKorkortstyp.VAR6.name(),
                BedomningKorkortstyp.VAR7.name(), BedomningKorkortstyp.VAR8.name(), BedomningKorkortstyp.VAR9.name(),
                BedomningKorkortstyp.VAR10.name()
            ),
            List.of(
                BedomningKorkortstyp.VAR1.name(), BedomningKorkortstyp.VAR2.name(),
                BedomningKorkortstyp.VAR3.name(), BedomningKorkortstyp.VAR4.name(), BedomningKorkortstyp.VAR5.name(),
                BedomningKorkortstyp.VAR6.name(), BedomningKorkortstyp.VAR7.name(), BedomningKorkortstyp.VAR8.name(),
                BedomningKorkortstyp.VAR9.name(), BedomningKorkortstyp.VAR10.name()
            ));
    }

    private static CertificateDataValueCodeList getDataValueCodeListMaximumValuesIntygetAvser() {
        return getDataValueCodeListMaximumValues(
            List.of(
                IntygAvserKategori.IAV1.name(),
                IntygAvserKategori.IAV2.name(),
                IntygAvserKategori.IAV3.name(),
                IntygAvserKategori.IAV4.name(),
                IntygAvserKategori.IAV5.name(),
                IntygAvserKategori.IAV6.name(),
                IntygAvserKategori.IAV7.name(),
                IntygAvserKategori.IAV8.name(),
                IntygAvserKategori.IAV9.name(),
                IntygAvserKategori.IAV10.name()
            ),
            List.of(
                IntygAvserKategori.IAV1.name(),
                IntygAvserKategori.IAV2.name(),
                IntygAvserKategori.IAV3.name(),
                IntygAvserKategori.IAV4.name(),
                IntygAvserKategori.IAV5.name(),
                IntygAvserKategori.IAV6.name(),
                IntygAvserKategori.IAV7.name(),
                IntygAvserKategori.IAV8.name(),
                IntygAvserKategori.IAV9.name(),
                IntygAvserKategori.IAV10.name()
            ));
    }

    private static CertificateDataValueVisualAcuities getCertificateDataValueVisualAcuities() {
        return CertificateDataValueVisualAcuities.builder()
            .rightEye(
                CertificateDataValueVisualAcuity.builder()
                    .withCorrection(CertificateDataValueDouble.builder()
                        .id(HOGER_OGA_MED_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .withoutCorrection(CertificateDataValueDouble.builder()
                        .id(HOGER_OGA_UTAN_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .contactLenses(CertificateDataValueBoolean.builder()
                        .id(KONTAKTLINSER_HOGER_OGA_DELSVAR_JSON_ID)
                        .selected(true)
                        .build())
                    .build()
            )
            .leftEye(
                CertificateDataValueVisualAcuity.builder()
                    .withCorrection(CertificateDataValueDouble.builder()
                        .id(VANSTER_OGA_MED_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .withoutCorrection(CertificateDataValueDouble.builder()
                        .id(VANSTER_OGA_UTAN_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .contactLenses(CertificateDataValueBoolean.builder()
                        .id(KONTAKTLINSER_VANSTER_OGA_JSON_ID)
                        .selected(true)
                        .build())
                    .build()
            )
            .binocular(
                CertificateDataValueVisualAcuity.builder()
                    .withCorrection(CertificateDataValueDouble.builder()
                        .id(BINOKULART_MED_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .withoutCorrection(CertificateDataValueDouble.builder()
                        .id(BINOKULART_UTAN_KORREKTION_JSON_ID)
                        .value(2.0)
                        .build())
                    .build()
            )
            .build();
    }
}
