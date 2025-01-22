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

import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVAILABLE_FUNCTION_CUSTOMIZE_BODY;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVAILABLE_FUNCTION_CUSTOMIZE_DESCRIPTION;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVAILABLE_FUNCTION_CUSTOMIZE_NAME;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVAILABLE_FUNCTION_CUSTOMIZE_TITLE;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVAILABLE_FUNCTION_PRINT_NAME;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVSTANGNING_SMITTSKYDD_INFO_BODY;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVSTANGNING_SMITTSKYDD_INFO_NAME;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.AVSTANGNING_SMITTSKYDD_INFO_TITLE;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.HIDE_DIAGNOSIS_TEXT;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.OPTIONAL_FIELD_DIAGNOSER_HIDE_ID;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.SEND_CERTIFICATE_BODY;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.SEND_CERTIFICATE_NAME;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.SEND_CERTIFICATE_TITLE;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionConstants.SHOW_DIAGNOSIS_TEXT;

import java.util.Collections;
import java.util.List;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationTypeDto;

public final class AvailableFunctionFactory {

    private AvailableFunctionFactory() {
        throw new IllegalStateException("Utility class!");
    }

    public static AvailableFunctionDTO avstangningSmittskydd(boolean enabled) {
        return AvailableFunctionDTO.create(
            AvailableFunctionTypeDTO.ATTENTION,
            AVSTANGNING_SMITTSKYDD_INFO_TITLE,
            AVSTANGNING_SMITTSKYDD_INFO_NAME,
            AVSTANGNING_SMITTSKYDD_INFO_BODY,
            enabled);
    }

    public static AvailableFunctionDTO customizePrint(boolean enabled, String fileName) {
        return AvailableFunctionDTO.create(
            AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE,
            AVAILABLE_FUNCTION_CUSTOMIZE_TITLE,
            AVAILABLE_FUNCTION_CUSTOMIZE_NAME,
            AVAILABLE_FUNCTION_CUSTOMIZE_BODY,
            AVAILABLE_FUNCTION_CUSTOMIZE_DESCRIPTION,
            List.of(
                InformationDTO.create(
                    fileName,
                    InformationTypeDto.FILENAME
                ),
                InformationDTO.create(
                    SHOW_DIAGNOSIS_TEXT,
                    InformationTypeDto.OPTIONS
                ),
                InformationDTO.create(
                    OPTIONAL_FIELD_DIAGNOSER_HIDE_ID,
                    HIDE_DIAGNOSIS_TEXT,
                    InformationTypeDto.OPTIONS
                )
            ),
            enabled
        );
    }

    public static AvailableFunctionDTO print(boolean enabled, String fileName) {
        return AvailableFunctionDTO.create(
            AvailableFunctionTypeDTO.PRINT_CERTIFICATE,
            "",
            AVAILABLE_FUNCTION_PRINT_NAME,
            "",
            "",
            List.of(
                InformationDTO.create(
                    fileName,
                    InformationTypeDto.FILENAME
                )
            ),
            enabled
        );
    }

    public static AvailableFunctionDTO send(boolean enabled) {
        return AvailableFunctionDTO.create(
            AvailableFunctionTypeDTO.SEND_CERTIFICATE,
            SEND_CERTIFICATE_TITLE,
            SEND_CERTIFICATE_NAME,
            SEND_CERTIFICATE_BODY,
            "",
            Collections.emptyList(),
            enabled
        );
    }
}
