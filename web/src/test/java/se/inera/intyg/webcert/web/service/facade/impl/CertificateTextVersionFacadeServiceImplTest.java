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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@ExtendWith(MockitoExtension.class)
class CertificateTextVersionFacadeServiceImplTest {

    @Spy
    private ObjectMapper mockObjectMapper;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private ModuleApi moduleApi;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private CertificateTextVersionFacadeServiceImpl certificateTextVersionFacadeService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String LATEST_VERSION = "1.3";
    private static final String PREVIOUS_VERSION = "1.2";

    @Nested
    class NoTextVersionUpdate {

        @Test
        public void shouldReturnNullIfUtkastIsNull() {
            final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(null);

            assertNull(actual);
            verifyNoInteractions(moduleApi);
            verifyNoInteractions(mockObjectMapper);
            verifyNoInteractions(utkastRepository);
        }

        @Test
        public void shoulNotUpdateIfDraftIsLockedl() throws JsonProcessingException {
            final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_LOCKED);
            final var expected = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_LOCKED);
            final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

            assertEquals(expected, actual);
            verifyNoInteractions(moduleApi);
            verifyNoInteractions(mockObjectMapper);
            verifyNoInteractions(utkastRepository);
        }

        @Test
        public void shoulNotUpdateIfDraftIsSignedl() throws JsonProcessingException {
            final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.SIGNED);
            final var expected = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.SIGNED);
            final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

            assertEquals(expected, actual);
            verifyNoInteractions(moduleApi);
            verifyNoInteractions(mockObjectMapper);
            verifyNoInteractions(utkastRepository);
        }
    }

    @Nested
    class TextVersionUpdatesAndExceptions {

        @BeforeEach
        public void setup() throws ModuleNotFoundException {
            doReturn(moduleApi).when(moduleRegistry).getModuleApi(anyString(), anyString());
            doReturn(LATEST_VERSION).when(intygTextsService).getLatestVersionForSameMajorVersion(anyString(), anyString());
        }

        @Test
        public void shouldNotUpdateIfAlreadyLatestVersion() throws IOException, ModuleException {
            doReturn(getUtlatande(LATEST_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());

            final var input = createUtkast(LATEST_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
            final var expected = createUtkast(LATEST_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
            final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

            assertTextVersionAndStatus(expected, actual);
            verifyNoInteractions(mockObjectMapper);
            verifyNoInteractions(utkastRepository);
        }


        @Nested
        class TextVersionUpdates {

            @BeforeEach
            public void setup() throws ModuleNotFoundException, IOException, ModuleException {
                when(utkastRepository.save(any(Utkast.class))).thenAnswer(i -> i.getArguments()[0]);
            }

            @Test
            public void shouldUpdateIfNotLatestObjectTextVersion() throws IOException, ModuleException {
                doReturn(getUtlatande(LATEST_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());

                final var input = createUtkast(PREVIOUS_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var expected = createUtkast(LATEST_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

                assertTextVersionAndStatus(expected, actual);
                verifyNoInteractions(mockObjectMapper);
                verify(utkastRepository, times(1)).save(any(Utkast.class));
            }

            @Test
            public void shouldUpdateIfNotLatestJsonModelTextVersion() throws IOException, ModuleException {
                doReturn(getUtlatande(PREVIOUS_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());

                final var input = createUtkast(LATEST_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_COMPLETE);
                final var expected = createUtkast(LATEST_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_COMPLETE);
                final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

                assertTextVersionAndStatus(expected, actual);
                verify(utkastRepository, times(1)).save(any(Utkast.class));
            }

            @Test
            public void shouldUpdateIfNotLatestObjectAndJsonModelTextVersion() throws IOException, ModuleException {
                doReturn(getUtlatande(PREVIOUS_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());

                final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var expected = createUtkast(LATEST_VERSION, LATEST_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

                assertTextVersionAndStatus(expected, actual);
                verify(utkastRepository, times(1)).save(any(Utkast.class));
            }
        }

        @Nested
        class Exceptions {

            @Test
            public void shouldThrowWebcertServiceExceptionIfOptimisticLockOnSave() throws IOException, ModuleException {
                doReturn(getUtlatande(PREVIOUS_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());
                doThrow(OptimisticLockingFailureException.class).when(utkastRepository).save(any(Utkast.class));

                final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var e = assertThrows(WebCertServiceException.class,
                    () -> certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input));

                assertEquals(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getErrorCode());
                verify(monitoringLogService).logUtkastConcurrentlyEdited(input.getIntygsId(), input.getIntygsTyp());
            }

            @Test
            public void shouldNotUpdateIfFailureUpdatingJsonModel() throws IOException, ModuleException {
                doReturn(getUtlatande(PREVIOUS_VERSION)).when(moduleApi).getUtlatandeFromJson(anyString());
                doThrow(JsonProcessingException.class).when(mockObjectMapper).readTree(anyString());

                final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var expected = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

                assertTextVersionAndStatus(expected, actual);
                verifyNoInteractions(utkastRepository);
            }

            @Test
            public void shouldNotUpdateIfModuleApiFailure() throws IOException, ModuleException {
                doThrow(IOException.class).when(moduleApi).getUtlatandeFromJson(anyString());

                final var input = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var expected = createUtkast(PREVIOUS_VERSION, PREVIOUS_VERSION, UtkastStatus.DRAFT_INCOMPLETE);
                final var actual = certificateTextVersionFacadeService.upgradeToLatestMinorTextVersion(input);

                assertTextVersionAndStatus(expected, actual);
                verifyNoInteractions(utkastRepository);
            }
        }
    }

    private void assertTextVersionAndStatus(Utkast expected, Utkast actual) {
        assertEquals(expected.getModel(), actual.getModel(), "Wrong text version in json model.");
        assertEquals(expected.getIntygTypeVersion(), actual.getIntygTypeVersion(), "Wrong text version in draft object");
        assertEquals(expected.getStatus(), actual.getStatus(), "Wrong certificate status");
    }

    private Utlatande getUtlatande(String textVersion) {
        final var builder = LisjpUtlatandeV1.builder();
        builder.setId("certificateId");
        builder.setGrundData(new GrundData());
        builder.setTextVersion(textVersion);
        return builder.build();
    }

    private Utkast createUtkast(String objectTextVersion, String jsonModelTextVersion, UtkastStatus status) throws JsonProcessingException {
        final var utkast = new Utkast();
        utkast.setIntygsId("certificateId");
        utkast.setIntygsTyp("certificateType");
        utkast.setIntygTypeVersion(objectTextVersion);
        utkast.setStatus(status);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
        utkast.setModel(objectMapper.writeValueAsString(getUtlatande(jsonModelTextVersion)));
        return utkast;
    }
}
