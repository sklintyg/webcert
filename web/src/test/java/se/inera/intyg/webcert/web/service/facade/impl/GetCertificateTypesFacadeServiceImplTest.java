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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.MissingRelatedCertificateConfirmation;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
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
    private FeaturesHelper featuresHelper;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private CertificateTypeMessageService certificateTypeMessageService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private MissingRelatedCertificateConfirmation missingRelatedCertificateConfirmation;

    @InjectMocks
    private GetCertificateTypesFacadeServiceImpl getCertificateTypesFacadeService;

    private static final String CERTIFICATE_TYPE_DB = "db";
    private static final String CERTIFICATE_TYPE = "id";
    private static final String CERTIFICATE_TYPE_VERSION = "1.0";
    private static final String PATIENT_NAME = "Förnamn Efternamn";
    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("19121212-1212").orElseThrow();

    @Nested
    class CorrectCases {

        private final IntygModule defaultModule = createIntygModule();
        private final IntygModule notAllowedModule = createIntygModule("notAllowed");
        private final IntygModule moduleDb = new IntygModule(CERTIFICATE_TYPE_DB, "label", "description", "detailedDescription",
            "issuerTypeId", "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient", CERTIFICATE_TYPE_DB);

        @BeforeEach
        void setup() {
            doReturn(CERTIFICATE_TYPE_VERSION)
                .when(intygTextsService)
                .getLatestVersion(anyString());

            final var user = mock(WebCertUser.class);
            doReturn(user)
                .when(webCertUserService)
                .getUser();
            doReturn("NORMAL")
                .when(user)
                .getOrigin();

            doReturn(Set.of(defaultModule.getId(), moduleDb.getId()))
                .when(authoritiesHelper)
                .getIntygstyperForPrivilege(any(), any());
        }

        @Nested
        class ModuleConversion {

            private List<CertificateTypeInfoDTO> types;

            @BeforeEach
            void setUp() {
                doReturn(Arrays.asList(defaultModule, notAllowedModule, moduleDb))
                    .when(intygModuleRegistry)
                    .listAllModules();
                final var patient = mock(Patient.class);
                when(patient.getFullstandigtNamn())
                    .thenReturn(PATIENT_NAME);
                when(patientDetailsResolver.resolvePatient(any(), anyString(), anyString()))
                    .thenReturn(patient);
            }

            @Test
            void shallConvertConfirmationModalIfProviderExists() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);

                assertNotNull(types.get(1).getConfirmationModal());
            }

            @Test
            void shallConvertConfirmationModalToNullIfNoProvider() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);

                assertNull(types.getFirst().getConfirmationModal());
            }

            @Test
            void shallConvertId() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(defaultModule.getId(), types.getFirst().getId());
            }

            @Test
            void shallConvertLabel() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(defaultModule.getLabel(), types.getFirst().getLabel());
            }

            @Test
            void shallConvertDescription() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(defaultModule.getDescription(), types.getFirst().getDescription());
            }

            @Test
            void shallConvertDetailedDescription() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(defaultModule.getDetailedDescription(), types.getFirst().getDetailedDescription());
            }

            @Test
            void shallConvertIssuerTypeId() {
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(defaultModule.getIssuerTypeId(), types.getFirst().getIssuerTypeId());
            }
        }

        @Nested
        class CertificateMessages {

            private List<CertificateTypeInfoDTO> types;

            @BeforeEach
            void setUp() {
                doReturn(Arrays.asList(defaultModule, notAllowedModule))
                    .when(intygModuleRegistry)
                    .listAllModules();
            }

            @Test
            void shallIncludeMessageWhenExists() {
                final var expectedMessage = "Message for certificate type";
                final var message = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_SAME_CARE_UNIT, expectedMessage);
                when(certificateTypeMessageService.get(CERTIFICATE_TYPE, PATIENT_ID)).thenReturn(Optional.of(message));
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(expectedMessage, types.getFirst().getMessage());
            }

            @Test
            void shallExludeMessageWhenItDoesntExists() {
                when(certificateTypeMessageService.get(CERTIFICATE_TYPE, PATIENT_ID)).thenReturn(Optional.empty());
                types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertNull(types.getFirst().getMessage());
            }
        }

        @Nested
        class ResourceLinks {

            @Test
            void shallConvertResourceLinks() {
                final var intygModule = createIntygModule();
                doReturn(List.of(intygModule))
                    .when(intygModuleRegistry)
                    .listAllModules();

                doAnswer(invocation -> {
                    List<IntygModuleDTO> dtos = invocation.getArgument(0);
                    dtos.forEach(dto -> dto.addLink(new ActionLink(ActionLinkType.SKAPA_UTKAST)));
                    return null;
                })
                    .when(resourceLinkHelper)
                    .decorateIntygModuleWithValidActionLinks(ArgumentMatchers.<List<IntygModuleDTO>>any(), any(Personnummer.class));

                final var types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(ResourceLinkTypeDTO.CREATE_CERTIFICATE, types.getFirst().getLinks().getFirst().getType());
                assertTrue(types.getFirst().getLinks().getFirst().isEnabled());
            }

            @Test
            void shallAddDisabledResourceLinkIfCreateCertificateIsUnavailable() {
                final var module = createIntygModule();
                doReturn(List.of(module))
                    .when(intygModuleRegistry)
                    .listAllModules();

                final var types = getCertificateTypesFacadeService.get(PATIENT_ID);
                assertEquals(ResourceLinkTypeDTO.CREATE_CERTIFICATE, types.getFirst().getLinks().getFirst().getType());
                assertFalse(types.getFirst().getLinks().getFirst().isEnabled());
            }

            @Test
            void shallAddCreateConfirmationResourceLinkIfCertificateIsLuaenaAndPatientOlderThanThirtyYearsAndTwoMonths() {
                final var module = createIntygModule("luae_na");
                doReturn(List.of(module)).when(intygModuleRegistry).listAllModules();
                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                final var types = getCertificateTypesFacadeService.get(getPatientId(3));
                assertEquals(1, types.getFirst().getLinks().stream()
                    .filter(link -> link.getType().equals(ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION)).count());
                assertTrue(types.getFirst().getLinks().stream().filter(link ->
                    link.getType() == ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION).findFirst().orElseThrow().isEnabled());
            }

            @Test
            void shallAddCreateConfirmationResourceLinkIfCertificateIsLuaenaAndPatientOlderThanThirtyYearsAndTwoMonthsWithCoordinationNo() {
                final var module = createIntygModule("luae_na");
                doReturn(List.of(module)).when(intygModuleRegistry).listAllModules();
                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                final var types = getCertificateTypesFacadeService.get(getPatientIdAsCoordinationNumber(3));
                assertEquals(1, types.getFirst().getLinks().stream()
                    .filter(link -> link.getType().equals(ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION)).count());
                assertTrue(types.getFirst().getLinks().stream().filter(link ->
                    link.getType() == ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION).findFirst().orElseThrow().isEnabled());
            }

            @Test
            void shallNotAddCreateConfirmationResourceLinkIfCertificateIsLuaenaAndPatientYoungerThanThirtyYearsAndTwoMonths() {
                final var module = createIntygModule("luae_na");
                doReturn(List.of(module)).when(intygModuleRegistry).listAllModules();
                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                final var types = getCertificateTypesFacadeService.get(getPatientId(1));
                assertTrue(types.getFirst().getLinks().stream()
                    .noneMatch(link -> link.getType().equals(ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION)));
            }

            @Test
            void shallNotAddCreateConfirmationResourceLinkIfCertificateIsLuaenaAndPatientYoungThanThirtyYearsAndTwoMonthsCoordinationNo() {
                final var module = createIntygModule("luae_na");
                doReturn(List.of(module)).when(intygModuleRegistry).listAllModules();
                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                final var types = getCertificateTypesFacadeService.get(getPatientIdAsCoordinationNumber(1));
                assertTrue(types.getFirst().getLinks().stream()
                    .noneMatch(link -> link.getType().equals(ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION)));
            }

            @Test
            void shallAddMissingRelatedCertificateConfirmationResourceLinkIfExists() {
                final var module = createIntygModule("doi");
                doReturn(List.of(module))
                    .when(intygModuleRegistry)
                    .listAllModules();

                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                doReturn(Optional.of(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION,
                        "name",
                        "description",
                        true)))
                    .when(missingRelatedCertificateConfirmation).get(module.getId(), PATIENT_ID);

                final var types = getCertificateTypesFacadeService.get(PATIENT_ID);

                assertEquals(ResourceLinkTypeDTO.CREATE_CERTIFICATE, types.getFirst().getLinks().getFirst().getType());
                assertEquals(ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION, types.getFirst().getLinks().get(1).getType());
            }

            @Test
            void shallNotAddMissingRelatedCertificateConfirmationResourceLinkDoesntExists() {
                final var module = createIntygModule("doi");
                doReturn(List.of(module))
                    .when(intygModuleRegistry)
                    .listAllModules();

                when(authoritiesHelper.getIntygstyperForPrivilege(any(), any())).thenReturn(Set.of(module.getId()));

                doReturn(Optional.empty())
                    .when(missingRelatedCertificateConfirmation).get(module.getId(), PATIENT_ID);

                final var types = getCertificateTypesFacadeService.get(PATIENT_ID);

                assertTrue(types.getFirst().getLinks().stream().noneMatch(
                        resourceLinkDTO -> resourceLinkDTO.getType() == ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION),
                    "Should not contain a ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION");
            }
        }

        @Nested
        class AuthorityCheck {

            @BeforeEach
            void setUp() {
                doReturn(Arrays.asList(defaultModule, notAllowedModule))
                    .when(intygModuleRegistry)
                    .listAllModules();
            }

            @Test
            void shallFilterCertificateTypesIfUserDoesNotHaveAuthority() {
                final var types = getCertificateTypesFacadeService.get(PATIENT_ID);

                assertEquals(1, types.size());
                assertEquals(CERTIFICATE_TYPE, types.getFirst().getId());
            }
        }
    }

    @Nested
    class DeprecatedIntygModules {

        WebCertUser user;

        void setup(boolean hasTextVersion) {
            final var module = createIntygModule();

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

            user = mock(WebCertUser.class);
            doReturn(user)
                .when(webCertUserService)
                .getUser();

            doReturn(Set.of(module.getId()))
                .when(authoritiesHelper)
                .getIntygstyperForPrivilege(any(), any());
        }

        @Test
        void shouldNotFilterOutIntygModulesWithTextVersion() {
            setup(true);
            doReturn("NORMAL")
                .when(user)
                .getOrigin();

            final var result = getCertificateTypesFacadeService.get(PATIENT_ID);

            assertEquals(1, result.size());
        }

        @Test
        void shouldNotFilterOutIntygModulesWithoutTextVersion() {
            setup(false);
            final var result = getCertificateTypesFacadeService.get(PATIENT_ID);
            assertEquals(0, result.size());
        }

        @Test
        void shouldFilterOutInactiveIntygModule() {
            setup(false);
            doReturn(List.of(CERTIFICATE_TYPE)).when(featuresHelper)
                .getCertificateTypesForFeature(AuthoritiesConstants.FEATURE_INACTIVE_CERTIFICATE_TYPE);
            final var result = getCertificateTypesFacadeService.get(PATIENT_ID);
            assertEquals(0, result.size());
        }
    }

    private IntygModule createIntygModule(String id) {
        return new IntygModule(id, "label", "description", "detailedDescription", "issuerTypeId",
            "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient", id);
    }

    private IntygModule createIntygModule() {
        return new IntygModule(CERTIFICATE_TYPE, "label", "description", "detailedDescription", "issuerTypeId",
            "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient", CERTIFICATE_TYPE);
    }

    private Personnummer getPatientId(int minusMonths) {
        final var patientBirthDate = LocalDate.now(ZoneId.systemDefault()).minusYears(30).minusMonths(minusMonths);
        final var patientId = patientBirthDate.toString().replace("-", "") + "4321";
        return Personnummer.createPersonnummer(patientId).orElseThrow();
    }

    private Personnummer getPatientIdAsCoordinationNumber(int minusMonths) {
        final var patientBirthDate = LocalDate.now(ZoneId.systemDefault()).minusYears(30).minusMonths(minusMonths)
            .format(DateTimeFormatter.BASIC_ISO_DATE);
        final var dayOfBirth = Integer.parseInt(patientBirthDate.substring(6, 8));
        final var coordinationNumber = patientBirthDate.substring(0, 6) + (dayOfBirth + 60) + "1234";
        return Personnummer.createPersonnummer(coordinationNumber).orElseThrow();
    }

}
