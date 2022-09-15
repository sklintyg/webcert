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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
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
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private IntygTextsService intygTextsService;

    @InjectMocks
    private GetCertificateTypesFacadeServiceImpl serviceUnderTest;

    private final static String PATIENT_ID = "191212121212";

    @Nested
    class ErrorCases {
        @Test
        void shallThrowExceptionForInvalidPatientId() {
            assertThrows(InvalidPersonNummerException.class, () -> serviceUnderTest.get("xxx"),
                    "Could not parse personnummer: xxx");
        }
    }

    @Nested
    class CorrectCases {
        private List<CertificateTypeInfoDTO> types;
        final private IntygModule module = createIntygModule();
        final private IntygModule notAllowedModule = createIntygModule("notAllowed");

        @BeforeEach
        void setup() throws Exception {
            doReturn(Arrays.asList(module, notAllowedModule))
                    .when(intygModuleRegistry)
                    .listAllModules();

            doReturn("1.0")
                    .when(intygTextsService)
                    .getLatestVersion(anyString());

            doNothing()
                    .when(resourceLinkHelper)
                    .decorateIntygModuleWithValidActionLinks(ArgumentMatchers.<List<IntygModuleDTO>>any(), any(Personnummer.class));

            final var user = mock(WebCertUser.class);
            doReturn(user)
                    .when(webCertUserService)
                    .getUser();

            doReturn(Set.of(module.getId()))
                    .when(authoritiesHelper)
                    .getIntygstyperForPrivilege(any(), any());

            types = serviceUnderTest.get(PATIENT_ID);
        }

        @Nested
        class ModuleConversion {

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

        @Nested
        class ResourceLinks {
            @Test
            void shallConvertResourceLinks() throws Exception {
                final var module = createIntygModule();
                doReturn(List.of(module))
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
                assertTrue(types.get(0).getLinks().get(0).isEnabled());
            }

            @Test
            void shallAddDisabledResourceLinkIfCreateCertificateIsUnavailable() throws Exception {
                final var module = createIntygModule();
                doReturn(List.of(module))
                        .when(intygModuleRegistry)
                        .listAllModules();

                final var types = serviceUnderTest.get(PATIENT_ID);
                assertEquals(ResourceLinkTypeDTO.CREATE_CERTIFICATE, types.get(0).getLinks().get(0).getType());
                assertFalse(types.get(0).getLinks().get(0).isEnabled());
            }
        }

        @Nested
        class AuthorityCheck {
            @Test
            void shallFilterCertificateTypesIfUserDoesNotHaveAuthority() throws InvalidPersonNummerException {
                final var types = serviceUnderTest.get(PATIENT_ID);

                assertEquals(1, types.size());
                assertEquals("id", types.get(0).getId());
            }
        }
    }

    @Nested
    class DeprecatedIntygModules {

        void setup(boolean isDeprecated, boolean showDeprecated, boolean hasTextVersion) throws Exception {
            final var module = createIntygModule(isDeprecated, showDeprecated);

            if (hasTextVersion) {
                doReturn("1.0")
                        .when(intygTextsService)
                        .getLatestVersion(anyString());
            }

            doReturn(List.of(module))
                    .when(intygModuleRegistry)
                    .listAllModules();

            doNothing()
                    .when(resourceLinkHelper)
                    .decorateIntygModuleWithValidActionLinks(ArgumentMatchers.<List<IntygModuleDTO>>any(), any(Personnummer.class));

            final var user = mock(WebCertUser.class);
            doReturn(user)
                    .when(webCertUserService)
                    .getUser();

            doReturn(Set.of(module.getId()))
                    .when(authoritiesHelper)
                    .getIntygstyperForPrivilege(any(), any());
        }

        @Test
        void shouldFilterOutDeprectatedIntygModules() throws Exception {
            setup(true, false, false);
            final var result = serviceUnderTest.get(PATIENT_ID);
            assertEquals(0, result.size());
        }

        @Test
        void shouldNotFilterOutDeprectatedIntygModulesIfShowDeprecated() throws Exception {
            setup(true, true, true);
            final var result = serviceUnderTest.get(PATIENT_ID);
            assertEquals(1, result.size());
        }

        @Test
        void shouldNotFilterOutIntygModulesWithoutTextVersion() throws Exception {
            setup(true, true, false);
            final var result = serviceUnderTest.get(PATIENT_ID);
            assertEquals(0, result.size());
        }
    }

    private IntygModule createIntygModule(String id) {
        return new IntygModule(id, "label", "description", "detailedDescription", "issuerTypeId",
                "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient",
                false, false);
    }

    private IntygModule createIntygModule() {
        return createIntygModule(false, false);
    }

    private IntygModule createIntygModule(boolean isDeprecated, boolean showDeprecated) {
        return new IntygModule("id", "label", "description", "detailedDescription", "issuerTypeId",
                "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient",
                isDeprecated, showDeprecated);
    }
}