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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static se.inera.intyg.common.ag7804.converter.RespConstants.DIAGNOS_SVAR_JSON_ID_6;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationTypeDto;

@Component
public class CertificateCustomizeFunction {

    private static final String AVAILABLE_FUNCTION_BODY = "När du skriver ut ett läkarintyg du ska lämna till din arbetsgivare kan du "
        + "välja om du vill att din diagnos ska visas eller döljas. Ingen annan information kan döljas. ";
    private static final String AVAILABLE_FUNCTION_TITLE = "Vill du visa eller dölja diagnos?";
    private static final String AVAILABLE_FUNCTION_NAME = "Anpassa intyget för utskrift";
    private static final String INFORMATION_ALERT_TEXT = "Information om diagnos kan vara viktig för din arbetsgivare."
        + " Det kan underlätta anpassning av din arbetssituation. Det kan också göra att du snabbare kommer tillbaka till arbetet.";
    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String OPTIONAL_FIELD_DIAGNOSER_SHOW_ID = DIAGNOS_SVAR_JSON_ID_6;
    private static final String OPTIONAL_FIELD_DIAGNOSER_HIDE_ID = "!" + DIAGNOS_SVAR_JSON_ID_6;
    private static final String SHOW_DIAGNOSIS_TEXT = "Visa Diagnos";
    private static final String HIDE_DIAGNOSIS_TEXT = "Dölj Diagnos";
    private static final String HIDE_DIAGNOSIS_ALERT_ID = "hideDiagnosisAlert";

    public Optional<AvailableFunctionDTO> get(Certificate certificate) {
        if (certificateIsCorrectType(certificate) && questionAvstangningSmittskyddIsNullOrFalse(certificate.getData())) {
            return Optional.of(
                AvailableFunctionDTO.create(
                    AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE,
                    AVAILABLE_FUNCTION_TITLE,
                    AVAILABLE_FUNCTION_NAME,
                    AVAILABLE_FUNCTION_BODY,
                    List.of(
                        InformationDTO.create(
                            OPTIONAL_FIELD_DIAGNOSER_SHOW_ID,
                            SHOW_DIAGNOSIS_TEXT,
                            InformationTypeDto.OPTIONS
                        ),
                        InformationDTO.create(
                            OPTIONAL_FIELD_DIAGNOSER_HIDE_ID,
                            HIDE_DIAGNOSIS_TEXT,
                            InformationTypeDto.OPTIONS
                        ),
                        InformationDTO.create(
                            HIDE_DIAGNOSIS_ALERT_ID,
                            INFORMATION_ALERT_TEXT,
                            InformationTypeDto.ALERT,
                            OPTIONAL_FIELD_DIAGNOSER_HIDE_ID
                        )
                    )
                )
            );
        }
        return Optional.empty();
    }

    private boolean questionAvstangningSmittskyddIsNullOrFalse(Map<String, CertificateDataElement> data) {
        if (!data.containsKey(AVSTANGNING_SMITTSKYDD_QUESTION_ID)) {
            return false;
        }
        final var value = (CertificateDataValueBoolean) data.get(AVSTANGNING_SMITTSKYDD_QUESTION_ID).getValue();
        return value.getSelected() == null || !value.getSelected();
    }

    private static boolean certificateIsCorrectType(Certificate certificate) {
        return certificate.getMetadata().getType().equals(Ag7804EntryPoint.MODULE_ID);
    }
}
