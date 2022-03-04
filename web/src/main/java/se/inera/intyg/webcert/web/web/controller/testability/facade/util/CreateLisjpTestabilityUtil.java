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
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.model.internal.ArbetslivsinriktadeAtgarder;
import se.inera.intyg.common.lisjp.model.internal.PrognosDagarTillArbeteTyp;
import se.inera.intyg.common.lisjp.model.internal.PrognosTyp;
import se.inera.intyg.common.lisjp.model.internal.Sysselsattning;
import se.inera.intyg.common.support.facade.model.value.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static se.inera.intyg.common.fkparent.model.converter.RespConstants.*;

@Component
public class CreateLisjpTestabilityUtil {

    private static String EXAMPLE_TEXT = "Detta är ett exempel";

    private final CreateCertificateValuesTestabilityUtil createCertificateValuesTestabilityUtil;

    @Autowired
    public CreateLisjpTestabilityUtil(CreateCertificateValuesTestabilityUtil createCertificateValuesTestabilityUtil) {
        this.createCertificateValuesTestabilityUtil = createCertificateValuesTestabilityUtil;
    }

    public Map<String, CertificateDataValue> createMinimumValuesLisjp() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataValueBoolean avstangningSmittskydd = CertificateDataValueBoolean.builder()
            .id(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27)
            .selected(true)
            .build();
        values.put(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_ID_27, avstangningSmittskydd);

        final CertificateDataValueDiagnosisList diagnos = createCertificateValuesTestabilityUtil.getCertificateDataValueDiagnosisList();
        values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

        final CertificateDataValueDateRangeList bedomning = createCertificateValuesTestabilityUtil
                .getCertificateDataValueDateRangeList(false);
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesLisjp() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var baseratPa = CertificateDataValueDateList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueDate.builder()
                                        .id(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANNAT_SVAR_JSON_ID_1)
                                        .date(LocalDate.now())
                                        .build()

                        )
                )
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1, baseratPa);

        final var motiveringAnnat = CertificateDataTextValue.builder()
                .id(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_BESKRIVNING_DELSVAR_JSON_ID_1)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TYP_DELSVAR_ID_1, motiveringAnnat);

        final var motiveringEjUndersokning = CertificateDataTextValue.builder()
                .id(MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_ID_1)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_DATUM_DELSVAR_ID_1, motiveringEjUndersokning);

        final var sysselsattning = CertificateDataValueCodeList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueCode.builder()
                                        .id(Sysselsattning.SysselsattningsTyp.NUVARANDE_ARBETE.getId())
                                        .code(Sysselsattning.SysselsattningsTyp.NUVARANDE_ARBETE.getId())
                                        .build()
                        )
                )
                .build();
        values.put(RespConstants.TYP_AV_SYSSELSATTNING_SVAR_ID_28, sysselsattning);

        final var arbetsuppgifter = CertificateDataTextValue.builder()
                .id(RespConstants.NUVARANDE_ARBETE_SVAR_JSON_ID_29)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.NUVARANDE_ARBETE_SVAR_ID_29, arbetsuppgifter);

        final var diagnos = createCertificateValuesTestabilityUtil
                .getCertificateDataValueDiagnosisList();
        values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

        final var funktionsnedsattning = CertificateDataIcfValue.builder()
                .id(RespConstants.FUNKTIONSNEDSATTNING_SVAR_JSON_ID_35)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.FUNKTIONSNEDSATTNING_SVAR_ID_35, funktionsnedsattning);

        final var aktivitetsbegransning = CertificateDataIcfValue.builder()
                .id(RespConstants.AKTIVITETSBEGRANSNING_SVAR_JSON_ID_17)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.AKTIVITETSBEGRANSNING_SVAR_ID_17, aktivitetsbegransning);

        final var pagaendeBehandling = CertificateDataTextValue.builder()
                .id(RespConstants.PAGAENDEBEHANDLING_SVAR_JSON_ID_19)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.PAGAENDEBEHANDLING_SVAR_ID_19, pagaendeBehandling);

        final var planeradBehandling = CertificateDataTextValue.builder()
                .id(RespConstants.PLANERADBEHANDLING_SVAR_JSON_ID_20)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.PLANERADBEHANDLING_SVAR_ID_20, planeradBehandling);

        final CertificateDataValueDateRangeList bedomning = createCertificateValuesTestabilityUtil
                .getCertificateDataValueDateRangeList(true);
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        final var motiveringTidigtStartdatum = CertificateDataTextValue.builder()
                .id(RespConstants.MOTIVERING_TILL_TIDIGT_STARTDATUM_FOR_SJUKSKRIVNING_ID)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_NIVA_DELSVARSVAR_ID_32, motiveringTidigtStartdatum);

        final var forsakringsMedicinsktBeslutsstod = CertificateDataTextValue.builder()
                .id(RespConstants.FORSAKRINGSMEDICINSKT_BESLUTSSTOD_SVAR_JSON_ID_37)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.FORSAKRINGSMEDICINSKT_BESLUTSSTOD_SVAR_ID_37, forsakringsMedicinsktBeslutsstod);

        final var arbetstidsforlaggning = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(RespConstants.ARBETSTIDSFORLAGGNING_SVAR_JSON_ID_33)
                .build();
        values.put(RespConstants.ARBETSTIDSFORLAGGNING_SVAR_ID_33, arbetstidsforlaggning);

        final var motiveringArbetstidsforlaggning = CertificateDataTextValue.builder()
                .id(RespConstants.ARBETSTIDSFORLAGGNING_MOTIVERING_SVAR_JSON_ID_33)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ARBETSTIDSFORLAGGNING_MOTIVERING_SVAR_ID_33, motiveringArbetstidsforlaggning);

        final var arbetsresor = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(RespConstants.ARBETSRESOR_SVAR_JSON_ID_34)
                .build();
        values.put(RespConstants.ARBETSRESOR_SVAR_ID_34, arbetsresor);

        final var prognos = CertificateDataValueCode.builder()
                .id(PrognosTyp.ATER_X_ANTAL_DGR.getId())
                .code(PrognosTyp.ATER_X_ANTAL_DGR.getId())
                .build();
        values.put(RespConstants.PROGNOS_SVAR_ID_39, prognos);

        final var prognosTimePeriod = CertificateDataValueCode.builder()
                .id(PrognosDagarTillArbeteTyp.DAGAR_30.getId())
                .code(PrognosDagarTillArbeteTyp.DAGAR_30.getId())
                .build();
        values.put(RespConstants.PROGNOS_BESKRIVNING_DELSVAR_ID_39, prognosTimePeriod);

        final var atgarder = CertificateDataValueCodeList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueCode.builder()
                                        .id(ArbetslivsinriktadeAtgarder.ArbetslivsinriktadeAtgarderVal.OVRIGT.getId())
                                        .code(ArbetslivsinriktadeAtgarder.ArbetslivsinriktadeAtgarderVal.OVRIGT.getId())
                                        .build()
                        )
                )
                .build();
        values.put(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_SVAR_ID_40, atgarder);

        final var atgarderBeskrivning = CertificateDataTextValue.builder()
                .id(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_BESKRIVNING_SVAR_JSON_ID_44)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_BESKRIVNING_SVAR_ID_44, atgarderBeskrivning);

        final var ovrigt = CertificateDataTextValue.builder()
                .id(RespConstants.OVRIGT_SVAR_JSON_ID_25)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.OVRIGT_SVAR_ID_25, ovrigt);

        final var kontakt = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(KONTAKT_ONSKAS_SVAR_JSON_ID_26)
                .build();
        values.put(KONTAKT_ONSKAS_SVAR_ID_26, kontakt);

        final var kontaktMotivering = CertificateDataTextValue.builder()
                .id(RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID_26)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_ID_26, kontaktMotivering);

        return values;
    }
}
