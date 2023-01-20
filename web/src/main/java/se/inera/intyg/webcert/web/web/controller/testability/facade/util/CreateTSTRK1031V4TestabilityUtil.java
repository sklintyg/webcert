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

import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_ANNAN_ANGE_VILKEN_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_ANNAN_ANGE_VILKEN_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_ANNAN_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_INSULIN_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BEHANDLING_TABLETTER_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BESKRIVNING_ANNAN_TYP_AV_DIABETES_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_BESKRIVNING_ANNAN_TYP_AV_DIABETES_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_DIABETES_DIAGNOS_AR_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_FOR_DIABETES_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_FOR_DIABETES_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_TIDPUNKT_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_TIDPUNKT_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_PATIENTEN_FOLJS_AV_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.ALLMANT_TYP_AV_DIABETES_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.BEDOMNING_OVRIGA_KOMMENTARER_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.BEDOMNING_OVRIGA_KOMMENTARER_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.BEDOMNING_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_TIDPUNKT_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_TIDPUNKT_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_KONTROLLERAS_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_KONTROLLERAS_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TIDPUNKT_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TIDPUNKT_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TRAFIK_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TRAFIK_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TOLV_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TOLV_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_TIDPUNKT_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_TIDPUNKT_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_FORMAGA_KANNA_VARNINGSTECKEN_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_FORMAGA_KANNA_VARNINGSTECKEN_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_FORSTAR_RISKER_MED_HYPOGLYKEMI_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_FORSTAR_RISKER_MED_HYPOGLYKEMI_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_VARFOR_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_VARFOR_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_REGELBUNDNA_BLODSOCKERKONTROLLER_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_REGELBUNDNA_BLODSOCKERKONTROLLER_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_VIDTA_ADEKVATA_ATGARDER_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.HYPOGLYKEMI_VIDTA_ADEKVATA_ATGARDER_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.IDENTITET_STYRKT_GENOM_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.INTYG_AVSER_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_BOR_UNDERSOKAS_AV_SPECIALIST_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_BOR_UNDERSOKAS_AV_SPECIALIST_SVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_ANGES_DELSVAR_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_ANGES_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_JSON_ID;
import static se.inera.intyg.common.ts_diabetes.v4.model.converter.RespConstants.OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_SVAR_ID;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueBoolean;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCode;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCodeList;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCodeListMaximumValues;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDate;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueText;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueYear;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.ts_diabetes.v4.model.internal.BedomningKorkortstyp;
import se.inera.intyg.common.ts_diabetes.v4.model.internal.IntygAvserKategori;
import se.inera.intyg.common.ts_diabetes.v4.model.kodverk.KvTypAvDiabetes;
import se.inera.intyg.common.ts_diabetes.v4.model.kodverk.KvVardniva;
import se.inera.intyg.common.ts_parent.codes.IdKontrollKod;

@Component
public class CreateTSTRK1031V4TestabilityUtil {

    public Map<String, CertificateDataValue> createMinimumValuesTSTRK1031V4() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var intygetAvser = getDataValueCodeList(IntygAvserKategori.VAR12.name(), IntygAvserKategori.VAR12.name());
        values.put(INTYG_AVSER_SVAR_ID, intygetAvser);

        final var identitetStyrktGenom = getDataValueCode(IdKontrollKod.ID_KORT.getCode(), IdKontrollKod.ID_KORT.getCode());
        values.put(IDENTITET_STYRKT_GENOM_SVAR_ID, identitetStyrktGenom);

        final var patientenFoljsAv = getDataValueCode(KvVardniva.PRIMARVARD.getCode(), KvVardniva.PRIMARVARD.getCode());
        values.put(ALLMANT_PATIENTEN_FOLJS_AV_SVAR_ID, patientenFoljsAv);

        final var diabetesDiagnosAr = getDataValueYear();
        values.put(ALLMANT_DIABETES_DIAGNOS_AR_SVAR_ID, diabetesDiagnosAr);

        final var diabetesTyp = getDataValueCode(KvTypAvDiabetes.TYP1.getCode(), KvTypAvDiabetes.TYP1.getCode());
        values.put(ALLMANT_TYP_AV_DIABETES_SVAR_ID, diabetesTyp);

        final var diabetesHarMedicinering = getDataValueBoolean(ALLMANT_MEDICINERING_FOR_DIABETES_JSON_ID, false);
        values.put(ALLMANT_MEDICINERING_FOR_DIABETES_SVAR_ID, diabetesHarMedicinering);

        final var ovrigtKomplikationer = getDataValueBoolean(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_JSON_ID, false);
        values.put(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_SVAR_ID, ovrigtKomplikationer);

        final var bedomningUppfyllerBehorighetskrav = getDataValueCodeList(BedomningKorkortstyp.VAR12.name(),
            BedomningKorkortstyp.VAR12.name());
        values.put(BEDOMNING_SVAR_ID, bedomningUppfyllerBehorighetskrav);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesTSTRK1031V4() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var intygetAvser = getDataValueCodeListMaximumValuesIntygetAvser();
        values.put(INTYG_AVSER_SVAR_ID, intygetAvser);

        final var identitetStyrktGenom = getDataValueCode(IdKontrollKod.ID_KORT.getCode(), IdKontrollKod.ID_KORT.getCode());
        values.put(IDENTITET_STYRKT_GENOM_SVAR_ID, identitetStyrktGenom);

        final var patientenFoljsAv = getDataValueCode(KvVardniva.PRIMARVARD.getCode(), KvVardniva.PRIMARVARD.getCode());
        values.put(ALLMANT_PATIENTEN_FOLJS_AV_SVAR_ID, patientenFoljsAv);

        final var diabetesDiagnosAr = getDataValueYear();
        values.put(ALLMANT_DIABETES_DIAGNOS_AR_SVAR_ID, diabetesDiagnosAr);

        final var diabetesTyp = getDataValueCode(KvTypAvDiabetes.ANNAN.getCode(), KvTypAvDiabetes.ANNAN.getCode());
        values.put(ALLMANT_TYP_AV_DIABETES_SVAR_ID, diabetesTyp);

        final var annanBeskrivning = getDataValueText(ALLMANT_BESKRIVNING_ANNAN_TYP_AV_DIABETES_JSON_ID,
            "beskrivning av annan typ av diabetes");
        values.put(ALLMANT_BESKRIVNING_ANNAN_TYP_AV_DIABETES_DELSVAR_ID, annanBeskrivning);

        final var diabetesHarMedicinering = getDataValueBoolean(ALLMANT_MEDICINERING_FOR_DIABETES_JSON_ID, true);
        values.put(ALLMANT_MEDICINERING_FOR_DIABETES_SVAR_ID, diabetesHarMedicinering);

        final var rifkForHypogykemi = getDataValueBoolean(ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_JSON_ID, true);
        values.put(ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_SVAR_ID, rifkForHypogykemi);

        final var diabetesBehandling = getDataValueCodeListMaximumValues(
            List.of(
                ALLMANT_BEHANDLING_INSULIN_JSON_ID,
                ALLMANT_BEHANDLING_TABLETTER_JSON_ID,
                ALLMANT_BEHANDLING_ANNAN_JSON_ID
            ),
            List.of(
                ALLMANT_BEHANDLING_INSULIN_JSON_ID,
                ALLMANT_BEHANDLING_TABLETTER_JSON_ID,
                ALLMANT_BEHANDLING_ANNAN_JSON_ID
            )
        );
        values.put(ALLMANT_BEHANDLING_SVAR_ID, diabetesBehandling);

        final var diabetesHypoglykemiRiskDatum = getDataValueDate(ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_TIDPUNKT_JSON_ID,
            LocalDate.now());
        values.put(ALLMANT_MEDICINERING_MEDFOR_RISK_FOR_HYPOGYKEMI_TIDPUNKT_SVAR_ID, diabetesHypoglykemiRiskDatum);

        final var angeVilkenAnnanBehandling = getDataValueText(ALLMANT_BEHANDLING_ANNAN_ANGE_VILKEN_JSON_ID, "En annan behandling");
        values.put(ALLMANT_BEHANDLING_ANNAN_ANGE_VILKEN_DELSVAR_ID, angeVilkenAnnanBehandling);

        final var hypoglykemiSjukdomstillstand = getDataValueBoolean(HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_JSON_ID, false);
        values.put(HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_SVAR_ID, hypoglykemiSjukdomstillstand);

        final var hypglykemiVarfor = getDataValueText(HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_VARFOR_JSON_ID, "Varför?");
        values.put(HYPOGLYKEMI_KONTROLL_SJUKDOMSTILLSTAND_VARFOR_DELSVAR_ID, hypglykemiVarfor);

        final var forstarRisker = getDataValueBoolean(HYPOGLYKEMI_FORSTAR_RISKER_MED_HYPOGLYKEMI_JSON_ID, true);
        values.put(HYPOGLYKEMI_FORSTAR_RISKER_MED_HYPOGLYKEMI_SVAR_ID, forstarRisker);

        final var hypglykemiFormagaKannaVarningstecken = getDataValueBoolean(HYPOGLYKEMI_FORMAGA_KANNA_VARNINGSTECKEN_JSON_ID, true);
        values.put(HYPOGLYKEMI_FORMAGA_KANNA_VARNINGSTECKEN_SVAR_ID, hypglykemiFormagaKannaVarningstecken);

        final var hypglykemiVidtaAdekvataAtgarder = getDataValueBoolean(HYPOGLYKEMI_VIDTA_ADEKVATA_ATGARDER_JSON_ID, true);
        values.put(HYPOGLYKEMI_VIDTA_ADEKVATA_ATGARDER_SVAR_ID, hypglykemiVidtaAdekvataAtgarder);

        final var hypglykemiAterkommandeSenasteAret = getDataValueBoolean(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_JSON_ID, true);
        values.put(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_SVAR_ID, hypglykemiAterkommandeSenasteAret);

        final var hypglykemiAterkommandeSenasteAretTidpunkt = getDataValueDate(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TIDPUNKT_JSON_ID,
            LocalDate.now());
        values.put(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TIDPUNKT_DELSVAR_ID, hypglykemiAterkommandeSenasteAretTidpunkt);

        final var hypglykemiAterkommandeSenasteAretKontrolleras = getDataValueBoolean(
            HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_KONTROLLERAS_JSON_ID, true);
        values.put(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_KONTROLLERAS_DELSVAR_ID, hypglykemiAterkommandeSenasteAretKontrolleras);

        final var hypglykemiAterkommandeSenasteAretTrafik = getDataValueBoolean(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TRAFIK_JSON_ID, true);
        values.put(HYPOGLYKEMI_ATERKOMMANDE_SENASTE_ARET_TRAFIK_DELSVAR_ID, hypglykemiAterkommandeSenasteAretTrafik);

        final var hypglykemiAterkommandeVaketSenasteTolv = getDataValueBoolean(HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TOLV_JSON_ID, true);
        values.put(HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TOLV_SVAR_ID, hypglykemiAterkommandeVaketSenasteTolv);

        final var hypglykemiAterkommandeVaketSenasteTre = getDataValueBoolean(HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_JSON_ID, true);
        values.put(HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_DELSVAR_ID, hypglykemiAterkommandeVaketSenasteTre);

        final var hypglykemiAterkommandeVaketSenasteTreTidpunkt = getDataValueDate(
            HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_TIDPUNKT_JSON_ID, LocalDate.now());
        values.put(HYPOGLYKEMI_ATERKOMMANDE_VAKET_SENASTE_TRE_TIDPUNKT_DELSVAR_ID, hypglykemiAterkommandeVaketSenasteTreTidpunkt);

        final var hypglykemiAllvarligSenasteTolvManderna = getDataValueBoolean(HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_JSON_ID, true);
        values.put(HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_SVAR_ID, hypglykemiAllvarligSenasteTolvManderna);

        final var hypglykemiAllvarligSenasteTolvManadernaTidpunkt = getDataValueDate(
            HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_TIDPUNKT_JSON_ID, LocalDate.now());
        values.put(HYPOGLYKEMI_ALLVARLIG_SENASTE_TOLV_MANADERNA_TIDPUNKT_DELSVAR_ID, hypglykemiAllvarligSenasteTolvManadernaTidpunkt);

        final var hypglykemiRegelbundnaBlodsockerkontroller = getDataValueBoolean(HYPOGLYKEMI_REGELBUNDNA_BLODSOCKERKONTROLLER_JSON_ID,
            true);
        values.put(HYPOGLYKEMI_REGELBUNDNA_BLODSOCKERKONTROLLER_SVAR_ID, hypglykemiRegelbundnaBlodsockerkontroller);

        final var ovrigtKomplikationer = getDataValueBoolean(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_JSON_ID, true);
        values.put(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_SVAR_ID, ovrigtKomplikationer);

        final var komplikationer = getDataValueText(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_ANGES_JSON_ID, "Komplikationer");
        values.put(OVRIGT_KOMPLIKATIONER_AV_SJUKDOMEN_ANGES_DELSVAR_ID, komplikationer);

        final var undersokasAvSpecialist = getDataValueText(OVRIGT_BOR_UNDERSOKAS_AV_SPECIALIST_JSON_ID, "Undersökas av specialist");
        values.put(OVRIGT_BOR_UNDERSOKAS_AV_SPECIALIST_SVAR_ID, undersokasAvSpecialist);

        final var ovrigaKommentarer = getDataValueText(BEDOMNING_OVRIGA_KOMMENTARER_JSON_ID, "Övriga kommentarer");
        values.put(BEDOMNING_OVRIGA_KOMMENTARER_SVAR_ID, ovrigaKommentarer);

        final var bedomning = getDataValueCodeListMaximumValuesBedomning();
        values.put(BEDOMNING_SVAR_ID, bedomning);

        return values;
    }

    private static CertificateDataValueCodeList getDataValueCodeListMaximumValuesBedomning() {
        return getDataValueCodeListMaximumValues(
            List.of(
                BedomningKorkortstyp.VAR1.name(), BedomningKorkortstyp.VAR2.name(),
                BedomningKorkortstyp.VAR3.name(), BedomningKorkortstyp.VAR4.name(), BedomningKorkortstyp.VAR5.name(),
                BedomningKorkortstyp.VAR6.name(), BedomningKorkortstyp.VAR7.name(), BedomningKorkortstyp.VAR8.name(),
                BedomningKorkortstyp.VAR9.name(), BedomningKorkortstyp.VAR15.name(),
                BedomningKorkortstyp.VAR12.name(), BedomningKorkortstyp.VAR13.name(), BedomningKorkortstyp.VAR14.name()
            ),
            List.of(
                BedomningKorkortstyp.VAR1.name(), BedomningKorkortstyp.VAR2.name(),
                BedomningKorkortstyp.VAR3.name(), BedomningKorkortstyp.VAR4.name(), BedomningKorkortstyp.VAR5.name(),
                BedomningKorkortstyp.VAR6.name(), BedomningKorkortstyp.VAR7.name(), BedomningKorkortstyp.VAR8.name(),
                BedomningKorkortstyp.VAR9.name(), BedomningKorkortstyp.VAR15.name(),
                BedomningKorkortstyp.VAR12.name(), BedomningKorkortstyp.VAR13.name(), BedomningKorkortstyp.VAR14.name())
        );
    }

    private static CertificateDataValueCodeList getDataValueCodeListMaximumValuesIntygetAvser() {
        return getDataValueCodeListMaximumValues(
            List.of(
                IntygAvserKategori.VAR1.name(),
                IntygAvserKategori.VAR2.name(),
                IntygAvserKategori.VAR3.name(),
                IntygAvserKategori.VAR4.name(),
                IntygAvserKategori.VAR5.name(),
                IntygAvserKategori.VAR6.name(),
                IntygAvserKategori.VAR7.name(),
                IntygAvserKategori.VAR8.name(),
                IntygAvserKategori.VAR9.name(),
                IntygAvserKategori.VAR12.name(),
                IntygAvserKategori.VAR13.name(),
                IntygAvserKategori.VAR14.name(),
                IntygAvserKategori.VAR15.name(),
                IntygAvserKategori.VAR16.name(),
                IntygAvserKategori.VAR17.name(),
                IntygAvserKategori.VAR18.name()
            ),
            List.of(
                IntygAvserKategori.VAR1.name(),
                IntygAvserKategori.VAR2.name(),
                IntygAvserKategori.VAR3.name(),
                IntygAvserKategori.VAR4.name(),
                IntygAvserKategori.VAR5.name(),
                IntygAvserKategori.VAR6.name(),
                IntygAvserKategori.VAR7.name(),
                IntygAvserKategori.VAR8.name(),
                IntygAvserKategori.VAR9.name(),
                IntygAvserKategori.VAR12.name(),
                IntygAvserKategori.VAR13.name(),
                IntygAvserKategori.VAR14.name(),
                IntygAvserKategori.VAR15.name(),
                IntygAvserKategori.VAR16.name(),
                IntygAvserKategori.VAR17.name(),
                IntygAvserKategori.VAR18.name()
            ));
    }
}
