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
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_UNDERSOKNING_AV_PATIENT_SVAR_JSON_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.AKTIVITETSBEGRANSNING_DELSVAR_ID_17;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.AKTIVITETSBEGRANSNING_SVAR_JSON_ID_17;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.DIAGNOSGRUND_NY_BEDOMNING_SVAR_JSON_ID_45;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.DIAGNOSGRUND_SVAR_ID_7;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.DIAGNOSGRUND_SVAR_JSON_ID_7;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.DIAGNOS_SVAR_ID_6;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_ID_8;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_JSON_ID_8;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KANNEDOM_SVAR_ID_2;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.KANNEDOM_SVAR_JSON_ID_2;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_ID_22;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_JSON_ID_22;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.NYDIAGNOS_SVAR_ID_45;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.SJUKDOMSFORLOPP_SVAR_ID_5;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.SJUKDOMSFORLOPP_SVAR_JSON_ID_5;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_ID_3;
import static se.inera.intyg.common.luae_na.v1.model.converter.RespConstants.UNDERLAGFINNS_SVAR_JSON_ID_3;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueBoolean;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDate;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDateListMinimal;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueMinimalDiagnosisListFk;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueText;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;

@Component
public class CreateLuaenaTestabilityUtil {

    public Map<String, CertificateDataValue> createMinimumValuesLuaena() {
        final var values = new HashMap<String, CertificateDataValue>();
        final var underlagBaseratPa = getDataValueDateListMinimal(GRUNDFORMEDICINSKTUNDERLAG_UNDERSOKNING_AV_PATIENT_SVAR_JSON_ID_1,
            LocalDate.now());
        values.put(GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1, underlagBaseratPa);

        final var kannedomOmPatienten = getDataValueDate(KANNEDOM_SVAR_JSON_ID_2, LocalDate.now());
        values.put(KANNEDOM_SVAR_ID_2, kannedomOmPatienten);

        final var underlagFinns = getDataValueBoolean(UNDERLAGFINNS_SVAR_JSON_ID_3, false);
        values.put(UNDERLAGFINNS_SVAR_ID_3, underlagFinns);

        final var diagnos = getDataValueMinimalDiagnosisListFk(DIAGNOSES_LIST_ITEM_1_ID,
            Diagnos.create("A00", "ICD-10-SE", "Kolera", "Kolera"));
        values.put(DIAGNOS_SVAR_ID_6, diagnos);

        final var diagnosBakgrund = getDataValueText(DIAGNOSGRUND_SVAR_JSON_ID_7, "diagnosBakgrund");
        values.put(DIAGNOSGRUND_SVAR_ID_7, diagnosBakgrund);

        final var nyBedomningDiagnosgrund = getDataValueBoolean(DIAGNOSGRUND_NY_BEDOMNING_SVAR_JSON_ID_45, false);
        values.put(NYDIAGNOS_SVAR_ID_45, nyBedomningDiagnosgrund);

        final var bakgrund = getDataValueText(SJUKDOMSFORLOPP_SVAR_JSON_ID_5, "bakgrund");
        values.put(SJUKDOMSFORLOPP_SVAR_ID_5, bakgrund);

        final var funktionsnedsattningIntellektuell = getDataValueText(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_JSON_ID_8,
            "funktionsnedsattning intellektuell");
        values.put(FUNKTIONSNEDSATTNING_INTELLEKTUELL_SVAR_ID_8, funktionsnedsattningIntellektuell);

        final var aktivitetsbegransning = getDataValueText(AKTIVITETSBEGRANSNING_SVAR_JSON_ID_17, "aktivitetsbegransning");
        values.put(AKTIVITETSBEGRANSNING_DELSVAR_ID_17, aktivitetsbegransning);

        final var medicinskaForutsattningarForArbete = getDataValueText(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_JSON_ID_22,
            "medicinska förutsättningar för arbete");
        values.put(MEDICINSKAFORUTSATTNINGARFORARBETE_SVAR_ID_22, medicinskaForutsattningarForArbete);

        return values;
    }
}
