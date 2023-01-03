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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromTemplateFacadeServiceImplTest {

    @Mock
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    @Mock
    private CopyUtkastService copyUtkastService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygTextsService intygTextsService;

    @InjectMocks
    private CreateCertificateFromTemplateFacadeServiceImpl createCertificateFromTemplateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String NEW_CERTIFICATE_ID = "renewCertificateId";
    private final static String CERTIFICATE_TYPE = "lisjp";
    private final static String NEW_CERTIFICATE_TYPE = "ag7804";
    private final static String PATIENT_ID = "191212121212";
    private final static String LATEST_VERSION = "2.0";

    @Nested
    class CreateFromTemplate {

        @BeforeEach
        void setUp() {
            setup(CERTIFICATE_TYPE, NEW_CERTIFICATE_TYPE);
        }

        @Test
        void shallIncludeCertificateId() {

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            final var certificateIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(certificateIdArgumentCaptor.capture(), anyString(), anyString(),
                    any(CopyIntygRequest.class));

            assertEquals(CERTIFICATE_ID, certificateIdArgumentCaptor.getValue());
        }

        @Test
        void shallIncludeNewCertificateType() {

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(anyString(), certificateTypeArgumentCaptor.capture(), anyString(),
                    any(CopyIntygRequest.class));

            assertEquals(NEW_CERTIFICATE_TYPE, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallIncludePatientId() {

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(anyString(), anyString(), anyString(),
                    renewIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallIncludeCorrectLatestVersion() {

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            final var renewIntygRequestArgumentCaptor = ArgumentCaptor.forClass(CopyIntygRequest.class);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(anyString(), anyString(), anyString(),
                    renewIntygRequestArgumentCaptor.capture());

            assertEquals(PATIENT_ID, renewIntygRequestArgumentCaptor.getValue().getPatientPersonnummer().getPersonnummer());
        }

        @Test
        void shallReturnNewDraftId() {

            final var actualCertificateId = createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            assertEquals(NEW_CERTIFICATE_ID, actualCertificateId);
        }
    }

    @Nested
    class SupportedCertificateTypes {

        @Test
        void shallSupportCreatingAg7804FromFk7804() {
            final var originalCertificateType = "lisjp";
            final var newCertificateType = "ag7804";
            setup(originalCertificateType, newCertificateType);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(anyString(), certificateTypeArgumentCaptor.capture(), anyString(),
                    any(CopyIntygRequest.class));

            assertEquals(newCertificateType, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallSupportCreatingDoiFromDb() {
            final var originalCertificateType = "db";
            final var newCertificateType = "doi";
            setup(originalCertificateType, newCertificateType);

            final var certificateTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID);

            verify(copyUtkastServiceHelper)
                .createUtkastFromDifferentIntygTypeRequest(anyString(), certificateTypeArgumentCaptor.capture(), anyString(),
                    any(CopyIntygRequest.class));

            assertEquals(newCertificateType, certificateTypeArgumentCaptor.getValue());
        }

        @Test
        void shallThrowExceptionWhenCertificateTypeNotSupported() {
            final var originalCertificateType = "orignalNotSupported";
            
            doReturn(createCertificate(originalCertificateType))
                .when(utkastService)
                .getDraft(eq(CERTIFICATE_ID), eq(Boolean.FALSE));

            assertThrows(IllegalArgumentException.class,
                () -> createCertificateFromTemplateFacadeService.createCertificateFromTemplate(CERTIFICATE_ID)
            );
        }
    }

    private void setup(String originalCertificateType, String newCertificateType) {
        doReturn(createCertificate(originalCertificateType))
            .when(utkastService)
            .getDraft(eq(CERTIFICATE_ID), eq(Boolean.FALSE));

        doReturn(LATEST_VERSION).when(intygTextsService).getLatestVersion(anyString());

        final var serviceRequest = new CreateUtkastFromTemplateRequest(
            CERTIFICATE_ID,
            newCertificateType,
            null,
            null,
            originalCertificateType
        );

        doReturn(serviceRequest)
            .when(copyUtkastServiceHelper)
            .createUtkastFromDifferentIntygTypeRequest(
                eq(CERTIFICATE_ID),
                eq(newCertificateType),
                eq(originalCertificateType),
                any(CopyIntygRequest.class)
            );

        final var serviceResponse = new CreateUtkastFromTemplateResponse(
            newCertificateType,
            LATEST_VERSION,
            NEW_CERTIFICATE_ID,
            CERTIFICATE_ID
        );

        doReturn(serviceResponse)
            .when(copyUtkastService)
            .createUtkastFromSignedTemplate(serviceRequest);
    }

    private Utkast createCertificate(String certificateType) {
        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);
        draft.setIntygsTyp(certificateType);
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.SIGNED);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }
}