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
import se.inera.intyg.common.support.facade.model.value.*;
import java.util.HashMap;
import java.util.Map;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.*;

@Component
public class CreateAf00213TestabilityUtil {

    private static String EXAMPLE_TEXT = "Detta är ett exempel";


    @Autowired
    public CreateAf00213TestabilityUtil() {
    }

    public Map<String, CertificateDataValue> createMinimumValuesAf00213() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataValueBoolean harFunktionsnedsattning = CertificateDataValueBoolean.builder()
                .id(FUNKTIONSNEDSATTNING_SVAR_JSON_ID_11)
                .selected(false)
                .build();
        values.put(FUNKTIONSNEDSATTNING_DELSVAR_ID_11, harFunktionsnedsattning);

        final CertificateDataValueBoolean harUtredningBehandling = CertificateDataValueBoolean.builder()
                .id(UTREDNING_BEHANDLING_SVAR_JSON_ID_31)
                .selected(false)
                .build();
        values.put(UTREDNING_BEHANDLING_DELSVAR_ID_31, harUtredningBehandling);

        final CertificateDataValueBoolean harArbetspaverkan = CertificateDataValueBoolean.builder()
                .id(ARBETETS_PAVERKAN_SVAR_JSON_ID_41)
                .selected(false)
                .build();
        values.put(ARBETETS_PAVERKAN_DELSVAR_ID_41, harArbetspaverkan);

        return values;
    }

    public Map<String, CertificateDataValue> createMaximumValuesAf00213() {
        final var values = new HashMap<String, CertificateDataValue>();

        final var harFunktionsnedsattning = CertificateDataValueBoolean.builder()
                .id(FUNKTIONSNEDSATTNING_SVAR_JSON_ID_11)
                .selected(true)
                .build();
        values.put(FUNKTIONSNEDSATTNING_DELSVAR_ID_11, harFunktionsnedsattning);

        final var funktionsnedsattning = CertificateDataTextValue.builder()
                .id(FUNKTIONSNEDSATTNING_SVAR_JSON_ID_12)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(FUNKTIONSNEDSATTNING_DELSVAR_ID_12, funktionsnedsattning);

        final var harAktivitetsbegransning = CertificateDataValueBoolean.builder()
                .id(AKTIVITETSBEGRANSNING_SVAR_JSON_ID_21)
                .selected(true)
                .build();
        values.put(AKTIVITETSBEGRANSNING_DELSVAR_ID_21, harAktivitetsbegransning);

        final var aktivitetsbegransning = CertificateDataTextValue.builder()
                .id(AKTIVITETSBEGRANSNING_SVAR_JSON_ID_22)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(AKTIVITETSBEGRANSNING_DELSVAR_ID_22, aktivitetsbegransning);

        final var harUtredningBehandling = CertificateDataValueBoolean.builder()
                .id(UTREDNING_BEHANDLING_SVAR_JSON_ID_31)
                .selected(true)
                .build();
        values.put(UTREDNING_BEHANDLING_DELSVAR_ID_31, harUtredningBehandling);

        final var utredningBehandling = CertificateDataTextValue.builder()
                .id(UTREDNING_BEHANDLING_SVAR_JSON_ID_32)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(UTREDNING_BEHANDLING_DELSVAR_ID_32, utredningBehandling);

        final var harArbetspaverkan = CertificateDataValueBoolean.builder()
                .id(ARBETETS_PAVERKAN_SVAR_JSON_ID_41)
                .selected(true)
                .build();
        values.put(ARBETETS_PAVERKAN_DELSVAR_ID_41, harArbetspaverkan);

        final var arbetspaverkan = CertificateDataTextValue.builder()
                .id(ARBETETS_PAVERKAN_SVAR_JSON_ID_42)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(ARBETETS_PAVERKAN_DELSVAR_ID_42, arbetspaverkan);

        final var ovrigt = CertificateDataTextValue.builder()
                .id(OVRIGT_SVAR_JSON_ID_5)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(OVRIGT_DELSVAR_ID_5, ovrigt);

        return values;
    }
}
