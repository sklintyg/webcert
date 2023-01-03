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

import static se.inera.intyg.common.sos_parent.support.RespConstants.ANTRAFFAT_DOD_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.ANTRAFFAT_DOD_DATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.BARN_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.BARN_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_OSAKERT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_SAKERT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSDATUM_SAKERT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_BOENDE_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_KOMMUN_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.DODSPLATS_KOMMUN_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.EXPLOSIV_AVLAGSNAT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.EXPLOSIV_AVLAGSNAT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.EXPLOSIV_IMPLANTAT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.EXPLOSIV_IMPLANTAT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.IDENTITET_STYRKT_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.IDENTITET_STYRKT_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.POLISANMALAN_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.POLISANMALAN_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.UNDERSOKNING_DATUM_DELSVAR_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.UNDERSOKNING_DATUM_JSON_ID;
import static se.inera.intyg.common.sos_parent.support.RespConstants.UNDERSOKNING_YTTRE_DELSVAR_ID;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.model.internal.Undersokning;
import se.inera.intyg.common.sos_parent.model.internal.DodsplatsBoende;
import se.inera.intyg.common.support.facade.model.value.CertificateDataTextValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataUncertainDateValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDate;

@Component
public class CreateDbTestabilityUtil {

    private final String kommun = "Östersund";
    private final String identitetStyrktText = "Körkort";
    private final LocalDate antraffadDodDate = LocalDate.of(2022, 5, 23);

    public Map<String, CertificateDataValue> createMinimumValuesDb() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataTextValue identitetStyrkt = CertificateDataTextValue.builder()
            .id(IDENTITET_STYRKT_JSON_ID)
            .text(identitetStyrktText)
            .build();
        values.put(IDENTITET_STYRKT_DELSVAR_ID, identitetStyrkt);

        final CertificateDataValueBoolean dodsdatumSakert = CertificateDataValueBoolean.builder()
            .id(DODSDATUM_SAKERT_JSON_ID)
            .selected(true)
            .build();
        values.put(DODSDATUM_SAKERT_DELSVAR_ID, dodsdatumSakert);

        final CertificateDataValueDate antraffadDod = CertificateDataValueDate.builder()
            .id(DODSDATUM_JSON_ID)
            .date(antraffadDodDate)
            .build();
        values.put(DODSDATUM_DELSVAR_ID, antraffadDod);

        final CertificateDataTextValue dodsplatsKommun = CertificateDataTextValue.builder()
            .id(DODSPLATS_KOMMUN_JSON_ID)
            .text(kommun)
            .build();
        values.put(DODSPLATS_KOMMUN_DELSVAR_ID, dodsplatsKommun);

        final CertificateDataValueCode dodsplatsBoende = CertificateDataValueCode.builder()
            .id(DODSPLATS_BOENDE_DELSVAR_ID)
            .code(DodsplatsBoende.ANNAN.name())
            .build();
        values.put(DODSPLATS_BOENDE_DELSVAR_ID, dodsplatsBoende);

        final CertificateDataValueBoolean barnQuestion = CertificateDataValueBoolean.builder()
            .id(BARN_JSON_ID)
            .selected(false)
            .build();
        values.put(BARN_DELSVAR_ID, barnQuestion);

        final CertificateDataValueBoolean explosivtImplantat = CertificateDataValueBoolean.builder()
            .id(EXPLOSIV_IMPLANTAT_JSON_ID)
            .selected(false)
            .build();
        values.put(EXPLOSIV_IMPLANTAT_DELSVAR_ID, explosivtImplantat);

        final CertificateDataValueCode yttreUndersokning = CertificateDataValueCode.builder()
            .id(Undersokning.JA.name())
            .code(Undersokning.JA.name())
            .build();
        values.put(UNDERSOKNING_YTTRE_DELSVAR_ID, yttreUndersokning);

        final CertificateDataValueBoolean polisanmalan = CertificateDataValueBoolean.builder()
            .id(POLISANMALAN_JSON_ID)
            .selected(true)
            .build();
        values.put(POLISANMALAN_DELSVAR_ID, polisanmalan);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesDb() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataTextValue identitetStyrkt = CertificateDataTextValue.builder()
            .id(IDENTITET_STYRKT_JSON_ID)
            .text(identitetStyrktText)
            .build();
        values.put(IDENTITET_STYRKT_DELSVAR_ID, identitetStyrkt);

        final CertificateDataValueBoolean dodsdatumSakert = CertificateDataValueBoolean.builder()
            .id(DODSDATUM_SAKERT_JSON_ID)
            .selected(false)
            .build();
        values.put(DODSDATUM_SAKERT_DELSVAR_ID, dodsdatumSakert);

        final CertificateDataUncertainDateValue osakertDatum = CertificateDataUncertainDateValue.builder()
            .id(DODSDATUM_JSON_ID)
            .value("2022-05-00")
            .build();
        values.put(DODSDATUM_OSAKERT_DELSVAR_ID, osakertDatum);

        final CertificateDataValueDate antraffadDod = CertificateDataValueDate.builder()
            .id(ANTRAFFAT_DOD_DATUM_JSON_ID)
            .date(antraffadDodDate)
            .build();
        values.put(ANTRAFFAT_DOD_DATUM_DELSVAR_ID, antraffadDod);

        final CertificateDataTextValue dodsplatsKommun = CertificateDataTextValue.builder()
            .id(DODSPLATS_KOMMUN_JSON_ID)
            .text(kommun)
            .build();
        values.put(DODSPLATS_KOMMUN_DELSVAR_ID, dodsplatsKommun);

        final CertificateDataValueCode dodsplatsBoende = CertificateDataValueCode.builder()
            .id(DODSPLATS_BOENDE_DELSVAR_ID)
            .code(DodsplatsBoende.ANNAN.name())
            .build();
        values.put(DODSPLATS_BOENDE_DELSVAR_ID, dodsplatsBoende);

        final CertificateDataValueBoolean barnQuestion = CertificateDataValueBoolean.builder()
            .id(BARN_JSON_ID)
            .selected(false)
            .build();
        values.put(BARN_DELSVAR_ID, barnQuestion);

        final CertificateDataValueBoolean explosivtImplantat = CertificateDataValueBoolean.builder()
            .id(EXPLOSIV_IMPLANTAT_JSON_ID)
            .selected(true)
            .build();
        values.put(EXPLOSIV_IMPLANTAT_DELSVAR_ID, explosivtImplantat);

        final CertificateDataValueBoolean explosivtImplantatAvlagsnat = CertificateDataValueBoolean.builder()
            .id(EXPLOSIV_AVLAGSNAT_JSON_ID)
            .selected(true)
            .build();
        values.put(EXPLOSIV_AVLAGSNAT_DELSVAR_ID, explosivtImplantatAvlagsnat);

        final CertificateDataValueCode yttreUndersokning = CertificateDataValueCode.builder()
            .id(Undersokning.UNDERSOKNING_GJORT_KORT_FORE_DODEN.name())
            .code(Undersokning.UNDERSOKNING_GJORT_KORT_FORE_DODEN.name())
            .build();
        values.put(UNDERSOKNING_YTTRE_DELSVAR_ID, yttreUndersokning);

        final CertificateDataValueDate undersokningsDatum = CertificateDataValueDate.builder()
            .id(UNDERSOKNING_DATUM_JSON_ID)
            .date(antraffadDodDate)
            .build();
        values.put(UNDERSOKNING_DATUM_DELSVAR_ID, undersokningsDatum);

        final CertificateDataValueBoolean polisanmalan = CertificateDataValueBoolean.builder()
            .id(POLISANMALAN_JSON_ID)
            .selected(true)
            .build();
        values.put(POLISANMALAN_DELSVAR_ID, polisanmalan);

        return values;
    }
}
