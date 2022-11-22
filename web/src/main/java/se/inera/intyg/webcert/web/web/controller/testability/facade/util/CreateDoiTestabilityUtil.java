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

import static se.inera.intyg.common.sos_parent.support.RespConstants.ANTRAFFAT_DOD_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.ANTRAFFAT_DOD_DATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.BARN_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.BARN_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.BIDRAGANDE_SJUKDOM_OM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_OSAKERT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_SAKERT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_SAKERT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSORSAK_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSORSAK_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_BOENDE_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_KOMMUN_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_KOMMUN_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_B_DATUM_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_B_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_C_DATUM_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_C_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_D_DATUM_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FOLJD_OM_DELSVAR_D_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_DATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_OM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_OM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_ORSAK_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_ORSAK_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_UPPKOMMELSE_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.FORGIFTNING_UPPKOMMELSE_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.GRUNDER_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.IDENTITET_STYRKT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.IDENTITET_STYRKT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.LAND_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.LAND_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.OPERATION_ANLEDNING_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.OPERATION_ANLEDNING_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.OPERATION_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.OPERATION_DATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.OPERATION_OM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.TERMINAL_DODSORSAK_JSON_ID;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataUncertainDateValue;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueBoolean;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCode;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueCodeList;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueDate;
import static se.inera.intyg.webcert.web.web.controller.testability.facade.util.DataValueUtil.getDataValueText;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.doi.model.internal.Dodsorsaksgrund;
import se.inera.intyg.common.doi.model.internal.ForgiftningOrsak;
import se.inera.intyg.common.doi.model.internal.OmOperation;
import se.inera.intyg.common.doi.model.internal.Specifikation;
import se.inera.intyg.common.sos_parent.model.internal.DodsplatsBoende;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCauseOfDeath;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCauseOfDeathList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;

@Component
public class CreateDoiTestabilityUtil {

    private static final String DESCRIPTION_ID = "description";
    private static final String DEBUT_ID = "debut";
    private final String kommun = "Östersund";
    private final String identitetStyrktText = "Körkort";

    public Map<String, CertificateDataValue> createMinimumValuesDoi() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var identitetStyrkt = getDataValueText(IDENTITET_STYRKT_JSON_ID, identitetStyrktText);
        values.put(IDENTITET_STYRKT_DELSVAR_ID, identitetStyrkt);

        final var dodsdatumSakert = getDataValueBoolean(DODSDATUM_SAKERT_JSON_ID, true);
        values.put(DODSDATUM_SAKERT_DELSVAR_ID, dodsdatumSakert);

        final var dodsdatum = getDataValueDate(DODSDATUM_JSON_ID, LocalDate.now());
        values.put(DODSDATUM_DELSVAR_ID, dodsdatum);

        final var dodsplatsKommun = getDataValueText(DODSPLATS_KOMMUN_JSON_ID, kommun);
        values.put(DODSPLATS_KOMMUN_DELSVAR_ID, dodsplatsKommun);

        final var dodsplatsBoende = getDataValueCode(DODSPLATS_BOENDE_DELSVAR_ID, DodsplatsBoende.ANNAN.name());
        values.put(DODSPLATS_BOENDE_DELSVAR_ID, dodsplatsBoende);

        final var barnQuestion = getDataValueBoolean(BARN_JSON_ID, false);
        values.put(BARN_DELSVAR_ID, barnQuestion);

        final var terminalDodsorsak = getDataValueCauseOfDeath(TERMINAL_DODSORSAK_JSON_ID, DODSORSAK_DELSVAR_ID,
            DODSORSAK_DATUM_DELSVAR_ID, "Smärta i knäet", LocalDate.now(), Specifikation.PLOTSLIG.name());
        values.put(DODSORSAK_DELSVAR_ID, terminalDodsorsak);

        final var operation = getDataValueCode(OmOperation.NEJ.name(), OmOperation.NEJ.name());
        values.put(OPERATION_OM_DELSVAR_ID, operation);

        final var forgiftning = getDataValueBoolean(FORGIFTNING_OM_JSON_ID, false);
        values.put(FORGIFTNING_OM_DELSVAR_ID, forgiftning);

        final var dodsorsaksUppgifter = getDataValueCodeList(Dodsorsaksgrund.RATTSMEDICINSK_BESIKTNING.name(),
            Dodsorsaksgrund.RATTSMEDICINSK_BESIKTNING.name());
        values.put(GRUNDER_DELSVAR_ID, dodsorsaksUppgifter);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesDoi() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var identitetStyrkt = getDataValueText(IDENTITET_STYRKT_JSON_ID, identitetStyrktText);
        values.put(IDENTITET_STYRKT_DELSVAR_ID, identitetStyrkt);

        final var spanien = "Spanien";
        final var land = getDataValueText(LAND_JSON_ID, spanien);
        values.put(LAND_DELSVAR_ID, land);

        final var dodsdatumSakert = getDataValueBoolean(DODSDATUM_SAKERT_JSON_ID, false);
        values.put(DODSDATUM_SAKERT_DELSVAR_ID, dodsdatumSakert);

        final var osakertDatum = getDataUncertainDateValue(DODSDATUM_JSON_ID, LocalDate.now().toString());
        values.put(DODSDATUM_OSAKERT_DELSVAR_ID, osakertDatum);

        final var antraffadDod = getDataValueDate(ANTRAFFAT_DOD_DATUM_JSON_ID, LocalDate.now().minusDays(10));
        values.put(ANTRAFFAT_DOD_DATUM_DELSVAR_ID, antraffadDod);

        final var dodsplatsKommun = getDataValueText(DODSPLATS_KOMMUN_JSON_ID, kommun);
        values.put(DODSPLATS_KOMMUN_DELSVAR_ID, dodsplatsKommun);

        final var dodsplatsBoende = getDataValueCode(DODSPLATS_BOENDE_DELSVAR_ID, DodsplatsBoende.ANNAN.name());
        values.put(DODSPLATS_BOENDE_DELSVAR_ID, dodsplatsBoende);

        final var barnQuestion = getDataValueBoolean(BARN_JSON_ID, false);
        values.put(BARN_DELSVAR_ID, barnQuestion);

        final var terminalDodsorsak = getDataValueCauseOfDeath(TERMINAL_DODSORSAK_JSON_ID, DODSORSAK_DELSVAR_ID,
            DODSORSAK_DATUM_DELSVAR_ID, "Smärta i knäet", LocalDate.now(), Specifikation.PLOTSLIG.name());
        values.put(DODSORSAK_DELSVAR_ID, terminalDodsorsak);

        final var foljdB = getDataValueCauseOfDeath(FOLJD_JSON_ID, FOLJD_OM_DELSVAR_B_ID,
            FOLJD_OM_DELSVAR_B_DATUM_ID, "Smärta i låret", LocalDate.now(), Specifikation.PLOTSLIG.name());
        values.put(FOLJD_OM_DELSVAR_B_ID, foljdB);

        final var foljdC = getDataValueCauseOfDeath(FOLJD_JSON_ID, FOLJD_OM_DELSVAR_C_ID,
            FOLJD_OM_DELSVAR_C_DATUM_ID, "Smärta i benet", LocalDate.now(), Specifikation.UPPGIFT_SAKNAS.name());
        values.put(FOLJD_OM_DELSVAR_C_ID, foljdC);

        final var foljdD = getDataValueCauseOfDeath(FOLJD_JSON_ID, FOLJD_OM_DELSVAR_D_ID,
            FOLJD_OM_DELSVAR_D_DATUM_ID, "Smärta i axlar", LocalDate.now(), Specifikation.KRONISK.name());
        values.put(FOLJD_OM_DELSVAR_D_ID, foljdD);

        final var bidragandeSjukdom = getDataValueCauseOfDeathList();
        values.put(BIDRAGANDE_SJUKDOM_OM_DELSVAR_ID, bidragandeSjukdom);

        final var operation = getDataValueCode(OmOperation.JA.name(), OmOperation.JA.name());
        values.put(OPERATION_OM_DELSVAR_ID, operation);

        final var operationsDatum = getDataValueDate(OPERATION_DATUM_JSON_ID, LocalDate.now());
        values.put(OPERATION_DATUM_DELSVAR_ID, operationsDatum);

        final var anledning = "Svårt att svälja";
        final var operationsAnledning = getDataValueText(OPERATION_ANLEDNING_JSON_ID, anledning);
        values.put(OPERATION_ANLEDNING_DELSVAR_ID, operationsAnledning);

        final var forgiftning = getDataValueBoolean(FORGIFTNING_OM_JSON_ID, true);
        values.put(FORGIFTNING_OM_DELSVAR_ID, forgiftning);

        final var forgiftningsOrsak = getDataValueCode(FORGIFTNING_ORSAK_JSON_ID, ForgiftningOrsak.OLYCKSFALL.name());
        values.put(FORGIFTNING_ORSAK_DELSVAR_ID, forgiftningsOrsak);

        final var forgiftningsDatum = getDataValueDate(FORGIFTNING_DATUM_JSON_ID, LocalDate.now());
        values.put(FORGIFTNING_DATUM_DELSVAR_ID, forgiftningsDatum);

        final var uppkommelseForgiftning = "Ormbett";
        final var forgiftningUppkommelse = getDataValueText(FORGIFTNING_UPPKOMMELSE_JSON_ID, uppkommelseForgiftning);
        values.put(FORGIFTNING_UPPKOMMELSE_DELSVAR_ID, forgiftningUppkommelse);

        final var dodsorsaksUppgifter = getDataValueCodeList(Dodsorsaksgrund.RATTSMEDICINSK_BESIKTNING.name(),
            Dodsorsaksgrund.RATTSMEDICINSK_BESIKTNING.name());
        values.put(GRUNDER_DELSVAR_ID, dodsorsaksUppgifter);

        return values;
    }

    private CertificateDataValueCauseOfDeathList getDataValueCauseOfDeathList() {
        return CertificateDataValueCauseOfDeathList.builder()
            .list(
                List.of(
                    getDataValueCauseOfDeath("0", DESCRIPTION_ID, DEBUT_ID, "Smärta i knäet", LocalDate.now(),
                        Specifikation.KRONISK.name()),
                    getDataValueCauseOfDeath("1", DESCRIPTION_ID, DEBUT_ID, "Ont i armen", LocalDate.now(),
                        Specifikation.PLOTSLIG.name()),
                    getDataValueCauseOfDeath("2", DESCRIPTION_ID, DEBUT_ID, "Magsmärta", LocalDate.now(),
                        Specifikation.KRONISK.name()),
                    getDataValueCauseOfDeath("3", DESCRIPTION_ID, DEBUT_ID, "Ont i huvudet", LocalDate.now(),
                        Specifikation.UPPGIFT_SAKNAS.name()),
                    getDataValueCauseOfDeath("4", DESCRIPTION_ID, DEBUT_ID, "Svårt att andas", LocalDate.now(),
                        Specifikation.PLOTSLIG.name()),
                    getDataValueCauseOfDeath("5", DESCRIPTION_ID, DEBUT_ID,
                        "Väldigt risigt i magen", LocalDate.now(),
                        Specifikation.PLOTSLIG.name()),
                    getDataValueCauseOfDeath("6", DESCRIPTION_ID, DEBUT_ID, "Ryggskada", LocalDate.now(),
                        Specifikation.KRONISK.name()),
                    getDataValueCauseOfDeath("7", DESCRIPTION_ID, DEBUT_ID, "Brutet långfinger", LocalDate.now(),
                        Specifikation.UPPGIFT_SAKNAS.name())
                )
            )
            .build();
    }


    private CertificateDataValueCauseOfDeath getDataValueCauseOfDeath(String id, String descriptionId, String datumId,
        String descriptionText, LocalDate date, String specifikationName) {
        return CertificateDataValueCauseOfDeath.builder()
            .id(id)
            .description(
                CertificateDataTextValue.builder()
                    .id(descriptionId)
                    .text(descriptionText)
                    .build()
            )
            .debut(
                CertificateDataValueDate.builder()
                    .id(datumId)
                    .date(date)
                    .build()
            )
            .specification(
                CertificateDataValueCode.builder()
                    .id(specifikationName)
                    .code(specifikationName)
                    .build()
            )
            .build();
    }
}
