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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@ExtendWith(MockitoExtension.class)
class GetCertificateTypesFacadeServiceImplTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;
    @Mock
    private ResourceLinkHelper resourceLinkHelper;

    @InjectMocks
    private GetCertificateTypesFacadeServiceImpl serviceUnderTest;

    private final static String PATIENT_ID = "191212121212";

    @Test
    void shallThrowExceptionForInvalidPatientId() {
        assertThrows(InvalidPersonNummerException.class, () -> serviceUnderTest.get("xxx"),
            "Could not parse personnummer: xxx");
    }

    @Nested
    class ModuleConversion {

        private List<CertificateTypeInfoDTO> types;
        private IntygModule module;

        @BeforeEach
        void setup() throws Exception {
            module = createIntygModule();

            doReturn(Arrays.asList(module))
                .when(intygModuleRegistry)
                .listAllModules();

            doNothing()
                .when(resourceLinkHelper)
                .decorateIntygModuleWithValidActionLinks(ArgumentMatchers.<List<IntygModuleDTO>>any(), any(Personnummer.class));

            types = serviceUnderTest.get(PATIENT_ID);
        }

        @Test
        void shallConvertId() {
            assertEquals(module.getId(), types.get(0).getId());
        }

        @Test
        void shallConvertLabel() {
            assertEquals(module.getLabel(), types.get(0).getLabel());
        }

        @Test
        void shallConvertDescription() {
            assertEquals(module.getDescription(), types.get(0).getDescription());
        }

        @Test
        void shallConvertDetailedDescription() {
            assertEquals(module.getDetailedDescription(), types.get(0).getDetailedDescription());
        }

        @Test
        void shallConvertIssuerTypeId() {
            assertEquals(module.getIssuerTypeId(), types.get(0).getIssuerTypeId());
        }
    }

    @Test
    void shallConvertResourceLinks() throws Exception {
        final var module = createIntygModule();
        doReturn(Arrays.asList(module))
            .when(intygModuleRegistry)
            .listAllModules();

        doAnswer(invocation -> {
            List<IntygModuleDTO> DTOs = invocation.getArgument(0);
            DTOs.forEach((DTO) -> DTO.addLink(new ActionLink(ActionLinkType.SKAPA_UTKAST)));
            return null;
        })
            .when(resourceLinkHelper)
            .decorateIntygModuleWithValidActionLinks(ArgumentMatchers.<List<IntygModuleDTO>>any(), any(Personnummer.class));

        final var types = serviceUnderTest.get(PATIENT_ID);
        assertEquals(ResourceLinkTypeDTO.CREATE_CERTIFICATE, types.get(0).getLinks().get(0).getType());
    }

    private IntygModule createIntygModule() {
        return new IntygModule("id", "label", "description", "detailedDescription", "issuerTypeId",
            "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient",
            false, false);
    }
}