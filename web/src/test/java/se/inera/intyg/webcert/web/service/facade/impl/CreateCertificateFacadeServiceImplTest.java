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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFacadeServiceImplTest {

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;
    @Mock
    private IntygModuleRegistry intygModuleRegistry;
    @Mock
    private AccessResultExceptionHelper accessResultExceptionHelper;
    @Mock
    private UtkastService utkastService;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private CreateCertificateFacadeServiceImpl serviceUnderTest;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CERTIFICATE_TYPE = "ag7804";
    private final static String PATIENT_ID = "191212121212";
    private final static String LATEST_VERSION = "2.0";

    @BeforeEach
    void setup() {
        doReturn(LATEST_VERSION)
            .when(intygTextsService)
            .getLatestVersion(eq(CERTIFICATE_TYPE));
    }

    @Nested
    class TestsWithUserMock {

        @BeforeEach
        void setup() {
            doReturn(new WebCertUser())
                .when(webCertUserService)
                .getUser();
        }

        @Nested
        class TestsWithPatientMock {

            @BeforeEach
            void setup() {
                doReturn(new Patient())
                    .when(patientDetailsResolver)
                    .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(LATEST_VERSION));
            }

            @Nested
            class TestsWithOkModule {

                @Test
                void shallThrowExceptionIfDraftIsNotUnique() {
                    doReturn(AccessResult.create(AccessResultCode.UNIQUE_DRAFT, "message"))
                        .when(draftAccessServiceHelper)
                        .evaluateAllowToCreateUtkast(eq(CERTIFICATE_TYPE), any(Personnummer.class));

                    assertThrows(CreateCertificateException.class,
                        () -> serviceUnderTest.create(CERTIFICATE_TYPE, PATIENT_ID),
                        "Certificate already exists");
                }

                @Test
                void shallThrowExceptionIfCertificateIsNotUnique() throws Exception {
                    doReturn(AccessResult.create(AccessResultCode.UNIQUE_CERTIFICATE, "message"))
                        .when(draftAccessServiceHelper)
                        .evaluateAllowToCreateUtkast(eq(CERTIFICATE_TYPE), any(Personnummer.class));

                    assertThrows(CreateCertificateException.class,
                        () -> serviceUnderTest.create(CERTIFICATE_TYPE, PATIENT_ID),
                        "Certificate already exists");
                }

                @Test
                void shallThrowExceptionIfCreateDraftIsNotAllowed() throws Exception {
                    doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_BLOCKED, "message"))
                        .when(draftAccessServiceHelper)
                        .evaluateAllowToCreateUtkast(eq(CERTIFICATE_TYPE), any(Personnummer.class));

                    doThrow(WebCertServiceException.class)
                        .when(accessResultExceptionHelper)
                        .throwException(any(AccessResult.class));

                    assertThrows(WebCertServiceException.class, () -> serviceUnderTest.create(CERTIFICATE_TYPE, PATIENT_ID));
                }

                @Test
                void shallReturnIdForSuccessfullyCreatedDraft() throws Exception {
                    doReturn(createCertificate())
                        .when(utkastService)
                        .createNewDraft(any(CreateNewDraftRequest.class));

                    doReturn(AccessResult.create(AccessResultCode.NO_PROBLEM, "message"))
                        .when(draftAccessServiceHelper)
                        .evaluateAllowToCreateUtkast(eq(CERTIFICATE_TYPE), any(Personnummer.class));

                    assertEquals(CERTIFICATE_ID, serviceUnderTest.create(CERTIFICATE_TYPE, PATIENT_ID));
                }
            }
        }

        @Test
        void shallThrowExceptionIfPatientDoesntExist() {
            doReturn(null)
                .when(patientDetailsResolver)
                .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(LATEST_VERSION));

            assertThrows(CreateCertificateException.class, () -> serviceUnderTest.create(CERTIFICATE_TYPE, PATIENT_ID),
                "Patient does not exist");
        }
    }


    @Test
    void shallThrowExceptionForInvalidPatientId() {
        assertThrows(CreateCertificateException.class, () -> serviceUnderTest.create(CERTIFICATE_TYPE, "xxx"),
            "Invalid patient id");
    }

    private IntygModule createIntygModule() {
        return new IntygModule("id", "label", "description", "detailedDescription", "issuerTypeId",
            "cssPath", "scriptPath", "dependencyDefinitionPath", "defaultRecipient");
    }

    private Utkast createCertificate() {
        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);
        draft.setIntygsTyp(CERTIFICATE_TYPE);
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_COMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        draft.setEnhetsId("enhetsId");
        return draft;
    }
}
