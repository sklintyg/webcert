/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static se.inera.intyg.common.ag7804.converter.RespConstants.DIAGNOS_SVAR_JSON_ID_6;

public final class AvailableFunctionConstants {

    private AvailableFunctionConstants() {
        throw new IllegalStateException("Utility class!");
    }

    public static final String AVSTANGNING_SMITTSKYDD_INFO_TITLE = "Avstängning enligt smittskyddslagen";
    public static final String AVSTANGNING_SMITTSKYDD_INFO_NAME = "Presentera informationsruta";
    public static final String AVSTANGNING_SMITTSKYDD_INFO_BODY = "I intyg som gäller avstängning enligt smittskyddslagen kan"
        + " du inte dölja din diagnos. När du klickar på 'Skriv ut' hämtas hela intyget.";
    public static final String AVAILABLE_FUNCTION_CUSTOMIZE_BODY =
        "När du skriver ut ett läkarintyg du ska lämna till din arbetsgivare kan du "
            + "välja om du vill att din diagnos ska visas eller döljas. Ingen annan information kan döljas. ";
    public static final String AVAILABLE_FUNCTION_CUSTOMIZE_TITLE = "Vill du visa eller dölja diagnos?";
    public static final String AVAILABLE_FUNCTION_CUSTOMIZE_DESCRIPTION = "Information om diagnos kan vara viktig för din arbetsgivare."
        + " Det kan underlätta anpassning av din arbetssituation. Det kan också göra att du snabbare kommer tillbaka till arbetet.";
    public static final String AVAILABLE_FUNCTION_CUSTOMIZE_NAME = "Anpassa intyget för utskrift";
    public static final String AVAILABLE_FUNCTION_PRINT_NAME = "Intyget kan skrivas ut";
    public static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    public static final String OPTIONAL_FIELD_DIAGNOSER_HIDE_ID = "!" + DIAGNOS_SVAR_JSON_ID_6;
    public static final String HIDE_DIAGNOSIS_TEXT = "Dölj Diagnos";
    public static final String HIDE_DIAGNOSIS_ALERT_ID = "hideDiagnosisAlert";
    public static final String OPTIONAL_FIELD_DIAGNOSER_SHOW_ID = DIAGNOS_SVAR_JSON_ID_6;
    public static final String SHOW_DIAGNOSIS_TEXT = "Visa Diagnos";
    public static final String SEND_CERTIFICATE_NAME = "Skicka intyg";
    public static final String SEND_CERTIFICATE_TITLE = "Skicka intyg";
    public static final String SEND_CERTIFICATE_BODY = "Från den här sidan kan du välja att skicka ditt intyg digitalt till mottagaren. "
        + "Endast mottagare som kan ta emot digitala intyg visas nedan.";
}
