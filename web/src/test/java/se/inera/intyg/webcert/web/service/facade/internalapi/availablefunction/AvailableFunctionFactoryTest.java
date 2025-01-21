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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.InformationTypeDto;

class AvailableFunctionFactoryTest {

    public static final String FILE_NAME = "fileName";

    @Nested
    class AvstangningSmittskyd {

        private final AvailableFunctionDTO avstangningSmittskydd = AvailableFunctionFactory.avstangningSmittskydd(true);

        @Test
        void shouldContainType() {
            assertEquals(AvailableFunctionTypeDTO.ATTENTION, avstangningSmittskydd.getType());
        }

        @Test
        void shouldContainTitle() {
            assertEquals(AVSTANGNING_SMITTSKYDD_INFO_TITLE, avstangningSmittskydd.getTitle());
        }

        @Test
        void shouldContainName() {
            assertEquals(AVSTANGNING_SMITTSKYDD_INFO_NAME, avstangningSmittskydd.getName());
        }

        @Test
        void shouldContainBody() {
            assertEquals(AVSTANGNING_SMITTSKYDD_INFO_BODY, avstangningSmittskydd.getBody());
        }

        @Test
        void shouldContainEnabled() {
            assertTrue(avstangningSmittskydd.isEnabled());
        }
    }

    @Nested
    class CustomizePrint {

        private final AvailableFunctionDTO customizePrint = AvailableFunctionFactory.customizePrint(true, FILE_NAME);

        @Test
        void shouldContainType() {
            assertEquals(AvailableFunctionTypeDTO.CUSTOMIZE_PRINT_CERTIFICATE, customizePrint.getType());
        }

        @Test
        void shouldContainTitle() {
            assertEquals(AVAILABLE_FUNCTION_CUSTOMIZE_TITLE, customizePrint.getTitle());
        }

        @Test
        void shouldContainName() {
            assertEquals(AVAILABLE_FUNCTION_CUSTOMIZE_NAME, customizePrint.getName());
        }

        @Test
        void shouldContainBody() {
            assertEquals(AVAILABLE_FUNCTION_CUSTOMIZE_BODY, customizePrint.getBody());
        }

        @Test
        void shouldContainDescription() {
            assertEquals(AVAILABLE_FUNCTION_CUSTOMIZE_DESCRIPTION, customizePrint.getDescription());
        }

        @Test
        void shouldContainListOfInformation() {
            final var expectedResult = List.of(
                InformationDTO.create(
                    FILE_NAME,
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
            );
            assertEquals(expectedResult, customizePrint.getInformation());
        }

        @Test
        void shouldContainEnabled() {
            assertTrue(customizePrint.isEnabled());
        }
    }

    @Nested
    class Print {

        private final AvailableFunctionDTO print = AvailableFunctionFactory.print(true, FILE_NAME);

        @Test
        void shouldContainType() {
            assertEquals(AvailableFunctionTypeDTO.PRINT_CERTIFICATE, print.getType());
        }

        @Test
        void shouldContainName() {
            assertEquals(AVAILABLE_FUNCTION_PRINT_NAME, print.getName());
        }

        @Test
        void shouldContainEnabled() {
            assertTrue(print.isEnabled());
        }

        @Test
        void shouldContainListOfInformation() {
            final var expectedResult = List.of(
                InformationDTO.create(
                    FILE_NAME,
                    InformationTypeDto.FILENAME
                )
            );
            assertEquals(expectedResult, print.getInformation());
        }
    }

    @Nested
    class Send {

        private final AvailableFunctionDTO sendEnabled = AvailableFunctionFactory.send(true);
        private final AvailableFunctionDTO sendDisabled = AvailableFunctionFactory.send(false);

        @Test
        void shouldContainType() {
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, sendEnabled.getType());
        }

        @Test
        void shouldContainTitle() {
            assertEquals(SEND_CERTIFICATE_TITLE, sendEnabled.getTitle());
        }

        @Test
        void shouldContainName() {
            assertEquals(SEND_CERTIFICATE_NAME, sendEnabled.getName());
        }

        @Test
        void shouldContainBody() {
            assertEquals(SEND_CERTIFICATE_BODY, sendEnabled.getBody());
        }

        @Test
        void shouldContainEnabled() {
            assertTrue(sendEnabled.isEnabled());
        }

        @Test
        void shouldContainDisabled() {
            assertFalse(sendDisabled.isEnabled());
        }
    }
}
