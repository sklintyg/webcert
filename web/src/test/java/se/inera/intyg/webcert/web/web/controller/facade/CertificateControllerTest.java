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
package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CopyCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCandidateMesssageForCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateResourceLinks;
import se.inera.intyg.webcert.web.service.facade.GetRelatedCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.UpdateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromCandidateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetCandidateMessageForCertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetRelatedCertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RenewCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;

@ExtendWith(MockitoExtension.class)
public class CertificateControllerTest {

    private static final String CERTIFICATE_ID = "XXXXXX-YYYYYYY-ZZZZZZZ-UUUUUUU";
    private static final long CERTIFICATE_VERSION = 1L;
    public static final String LAST_SAVED_DRAFT = "lastSavedDraft";
    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;
    @Mock
    private SaveCertificateFacadeService saveCertificateFacadeService;
    @Mock
    private ValidateCertificateFacadeService validationCertificateFacadeService;
    @Mock
    private SignCertificateFacadeService signCertificateFacadeService;
    @Mock
    private DeleteCertificateFacadeService deleteCertificateFacadeService;
    @Mock
    private RevokeCertificateFacadeService revokeCertificateFacadeService;
    @Mock
    private ReplaceCertificateFacadeService replaceCertificateFacadeService;
    @Mock
    private CopyCertificateFacadeService copyCertificateFacadeService;
    @Mock
    private RenewCertificateFacadeService renewCertificateFacadeService;
    @Mock
    private ForwardCertificateFacadeService forwardCertificateFacadeService;
    @Mock
    private ReadyForSignFacadeService readyForSignFacadeService;
    @Mock
    private GetCertificateEventsFacadeService getCertificateEventsFacadeService;
    @Mock
    private GetCertificateResourceLinks getCertificateResourceLinks;
    @Mock
    private SendCertificateFacadeService sendCertificateFacadeService;
    @Mock
    private ComplementCertificateFacadeService complementCertificateFacadeService;
    @Mock
    private CreateCertificateFromTemplateFacadeService createCertificateFromTemplateFacadeService;
    @Mock
    private UpdateCertificateFromCandidateFacadeService createCertificateFromCandiateFacadeService;

    @Mock
    private GetRelatedCertificateFacadeService getRelatedCertificateFacadeService;
    @Mock
    private GetCandidateMesssageForCertificateFacadeService getCandidateMesssageForCertificateFacadeService;
    @Mock
    private CreateCertificateFacadeService createCertificateFacadeService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CertificateController certificateController;

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id("191212121212")
                                    .build()
                            )
                            .build()
                    )
                    .unit(
                        Unit.builder()
                            .unitId("unitId")
                            .unitName("unitName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitId("careProviderId")
                            .unitName("careProviderName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .build()
            )
            .build();
    }

    @Nested
    class GetCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificate)
                .when(getCertificateFacadeService)
                .getCertificate(anyString(), anyBoolean(), eq(true));

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallPdlLog() {
            certificateController.getCertificate(CERTIFICATE_ID);

            final var pdlLogCaptor = ArgumentCaptor.forClass(Boolean.class);
            verify(getCertificateFacadeService).getCertificate(anyString(), pdlLogCaptor.capture(), eq(true));

            assertTrue(pdlLogCaptor.getValue(), "Should fetch certificate with pdlLog == true");
        }

        @Test
        void shallIncludeResourceLinks() {
            final var response = (CertificateResponseDTO) certificateController.getCertificate(CERTIFICATE_ID).getEntity();
            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class SaveCertificate {

        private Certificate certificate;
        private HttpServletRequest requestMock;
        private HttpSession sessionMock;

        @BeforeEach
        void setup() {
            certificate = createCertificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .build()
            );

            requestMock = mock(HttpServletRequest.class);
            sessionMock = mock(HttpSession.class);
            doReturn(sessionMock)
                .when(requestMock)
                .getSession(true);
        }

        @Test
        void shallPdlLogFirstSave() {
            doReturn(null).when(sessionMock).getAttribute(LAST_SAVED_DRAFT);

            certificateController.saveCertificate(CERTIFICATE_ID, certificate, requestMock);

            final var pdlLogCaptor = ArgumentCaptor.forClass(Boolean.class);
            verify(saveCertificateFacadeService).saveCertificate(eq(certificate), pdlLogCaptor.capture());

            assertTrue(pdlLogCaptor.getValue(), "Should save certificate with pdlLog == true");
        }

        @Test
        void shallUpdateSessionOnFirstSave() {
            certificateController.saveCertificate(CERTIFICATE_ID, certificate, requestMock);

            final var pdlLogCaptor = ArgumentCaptor.forClass(String.class);
            verify(sessionMock).setAttribute(eq(LAST_SAVED_DRAFT), pdlLogCaptor.capture());

            assertEquals(CERTIFICATE_ID, pdlLogCaptor.getValue());
        }

        @Test
        void shallNotPdlLogSubsequentSaves() {
            doReturn(CERTIFICATE_ID).when(sessionMock).getAttribute(LAST_SAVED_DRAFT);

            certificateController.saveCertificate(CERTIFICATE_ID, certificate, requestMock);

            final var pdlLogCaptor = ArgumentCaptor.forClass(Boolean.class);
            verify(saveCertificateFacadeService).saveCertificate(eq(certificate), pdlLogCaptor.capture());

            assertFalse(pdlLogCaptor.getValue(), "Should save certificate with pdlLog == true");
        }
    }

    @Nested
    class ValidateCertificate {

        private Certificate certificate;
        private ValidationErrorDTO[] validationErrors;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(validationErrors)
                .when(validationCertificateFacadeService)
                .validate(certificate);
        }

        @Test
        void shallReturnValidationErrors() {
            final var response = (ValidateCertificateResponseDTO) certificateController.validateCertificate(CERTIFICATE_ID, certificate)
                .getEntity();
            assertEquals(validationErrors, response.getValidationErrors());
        }
    }

    @Nested
    class SignCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificate)
                .when(signCertificateFacadeService)
                .signCertificate(certificate, "userIpAddress");

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallIncludeResourceLinks() {
            when(httpServletRequest.getRemoteAddr()).thenReturn("userIpAddress");
            try (final var response = certificateController.signCertificate(CERTIFICATE_ID, certificate, httpServletRequest)) {
                final var entity = (CertificateResponseDTO) response.getEntity();
                assertEquals(resourceLinks, entity.getCertificate().getLinks());
            }
        }
    }

    @Nested
    class DeleteCertificate {

        @Test
        void shallDeleteCertificate() {
            certificateController.deleteCertificate(CERTIFICATE_ID, CERTIFICATE_VERSION);

            final var certificateIdCaptor = ArgumentCaptor.forClass(String.class);
            final var certificateVersionCaptor = ArgumentCaptor.forClass(Long.class);

            verify(deleteCertificateFacadeService).deleteCertificate(certificateIdCaptor.capture(), certificateVersionCaptor.capture());

            assertEquals(CERTIFICATE_ID, certificateIdCaptor.getValue());
            assertEquals(CERTIFICATE_VERSION, certificateVersionCaptor.getValue());
        }
    }

    @Nested
    class RevokeCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificate)
                .when(revokeCertificateFacadeService)
                .revokeCertificate(anyString(), anyString(), anyString());

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallIncludeResourceLinks() {
            final var revokeCertificateRequestDTO = new RevokeCertificateRequestDTO();
            revokeCertificateRequestDTO.setReason("Reason");
            revokeCertificateRequestDTO.setMessage("Message");

            final var response = (CertificateResponseDTO) certificateController
                .revokeCertificate(CERTIFICATE_ID, revokeCertificateRequestDTO).getEntity();

            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class ReplaceCertificate {

        private Certificate certificate;

        @BeforeEach
        void setup() {
            certificate = createCertificate();
        }

        @Test
        void shallReturnIdOfNewCertificate() {
            final var expectedId = "newCertificateId";

            doReturn(expectedId)
                .when(replaceCertificateFacadeService)
                .replaceCertificate(anyString());

            final var newCertificateRequestDTO = new NewCertificateRequestDTO();
            newCertificateRequestDTO.setCertificateType("certificateType");
            newCertificateRequestDTO.setPatientId(certificate.getMetadata().getPatient().getPersonId());

            final var response = (ReplaceCertificateResponseDTO) certificateController
                .replaceCertificate(CERTIFICATE_ID, newCertificateRequestDTO)
                .getEntity();

            assertEquals(expectedId, response.getCertificateId());
        }
    }

    @Nested
    class RenewCertificate {

        private Certificate certificate;

        @BeforeEach
        void setup() {
            certificate = createCertificate();
        }

        @Test
        void shallReturnIdOfNewCertificate() {
            final var expectedId = "newCertificateId";

            doReturn(expectedId)
                .when(renewCertificateFacadeService)
                .renewCertificate(anyString());

            final var response = (RenewCertificateResponseDTO) certificateController.renewCertificate(CERTIFICATE_ID).getEntity();

            assertEquals(expectedId, response.getCertificateId());
        }
    }

    @Nested
    class ComplementCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;
        private ComplementCertificateRequestDTO complementCertificateRequest;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            complementCertificateRequest = new ComplementCertificateRequestDTO();
            complementCertificateRequest.setMessage("Message");

            doReturn(certificate)
                .when(complementCertificateFacadeService)
                .complement(CERTIFICATE_ID, complementCertificateRequest.getMessage());

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallReturnCertificate() {
            final var response = (CertificateResponseDTO) certificateController
                .complementCertificate(CERTIFICATE_ID, complementCertificateRequest).getEntity();

            assertNotNull(response.getCertificate());
        }

        @Test
        void shallReturnResourceLinks() {
            final var response = (CertificateResponseDTO) certificateController
                .complementCertificate(CERTIFICATE_ID, complementCertificateRequest).getEntity();

            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class AnswerComplementCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;
        private ComplementCertificateRequestDTO complementCertificateRequest;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            complementCertificateRequest = new ComplementCertificateRequestDTO();
            complementCertificateRequest.setMessage("Det går inte att komplettera");

            doReturn(certificate)
                .when(complementCertificateFacadeService)
                .answerComplement(CERTIFICATE_ID, complementCertificateRequest.getMessage());

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallReturnCertificate() {
            final var response = (CertificateResponseDTO) certificateController
                .answerComplementCertificate(CERTIFICATE_ID, complementCertificateRequest).getEntity();

            assertNotNull(response.getCertificate());
        }

        @Test
        void shallReturnResourceLinks() {
            final var response = (CertificateResponseDTO) certificateController
                .answerComplementCertificate(CERTIFICATE_ID, complementCertificateRequest).getEntity();

            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class CopyCertificate {

        private Certificate certificate;

        @BeforeEach
        void setup() {
            certificate = createCertificate();
        }

        @Test
        void shallReturnIdOfNewCertificate() {
            final var expectedId = "newCertificateId";

            doReturn(expectedId)
                .when(copyCertificateFacadeService)
                .copyCertificate(anyString());

            final var newCertificateRequestDTO = new NewCertificateRequestDTO();
            newCertificateRequestDTO.setCertificateType("certificateType");
            newCertificateRequestDTO.setPatientId(certificate.getMetadata().getPatient().getPersonId());

            final var response = (CopyCertificateResponseDTO) certificateController
                .copyCertificate(CERTIFICATE_ID, newCertificateRequestDTO)
                .getEntity();

            assertEquals(expectedId, response.getCertificateId());
        }
    }

    @Nested
    class CreateCertificateFromTemplate {

        @Test
        void shallReturnIdOfNewCertificate() {
            final var expectedId = "newCertificateId";

            doReturn(expectedId)
                .when(createCertificateFromTemplateFacadeService)
                .createCertificateFromTemplate(anyString());

            final var response = (CreateCertificateFromTemplateResponseDTO) certificateController
                .createCertificateFromTemplate(CERTIFICATE_ID).getEntity();

            assertEquals(expectedId, response.getCertificateId());
        }
    }

    @Nested
    class CreateCertificateFromCandidate {

        @Test
        void shallReturnIdOfNewCertificate() {
            final var expectedId = "newCertificateId";

            doReturn(expectedId)
                .when(createCertificateFromCandiateFacadeService)
                .update(anyString());

            final var response = (CreateCertificateFromCandidateResponseDTO) certificateController
                .updateCertificateFromCandidate(CERTIFICATE_ID).getEntity();

            assertEquals(expectedId, response.getCertificateId());
        }
    }

    @Nested
    class ForwardCertificate {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificate)
                .when(forwardCertificateFacadeService)
                .forwardCertificate(anyString(), anyBoolean());

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallIncludeResourceLinks() {
            final var forwardCertificateRequestDTO = new ForwardCertificateRequestDTO();
            forwardCertificateRequestDTO.setForward(true);

            final var response = (CertificateResponseDTO) certificateController
                .forwardCertificate(CERTIFICATE_ID, forwardCertificateRequestDTO).getEntity();

            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class ReadyForSign {

        private Certificate certificate;
        private ResourceLinkDTO[] resourceLinks;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificate)
                .when(readyForSignFacadeService)
                .readyForSign(anyString());

            resourceLinks = new ResourceLinkDTO[0];

            doReturn(resourceLinks)
                .when(getCertificateResourceLinks)
                .get(certificate);
        }

        @Test
        void shallIncludeResourceLinks() {
            final var response = (CertificateResponseDTO) certificateController.readyForSign(CERTIFICATE_ID).getEntity();

            assertEquals(resourceLinks, response.getCertificate().getLinks());
        }
    }

    @Nested
    class GetCertificateEvents {

        private Certificate certificate;
        private CertificateEventDTO[] certificateEvents;

        @BeforeEach
        void setup() {
            certificate = createCertificate();

            doReturn(certificateEvents)
                .when(getCertificateEventsFacadeService)
                .getCertificateEvents(anyString());
        }

        @Test
        void shallReturnCertificateEvents() {
            final var response = (CertificateEventResponseDTO) certificateController.getCertificateEvents(CERTIFICATE_ID).getEntity();
            assertEquals(certificateEvents, response.getCertificateEvents());
        }
    }

    @Nested
    class SendCertificate {

        @BeforeEach
        void setup() {
            when(sendCertificateFacadeService.sendCertificate(eq(CERTIFICATE_ID))).thenReturn(IntygServiceResult.OK.toString());
        }

        @Test
        void shallSendCertificate() {
            var result = (SendCertificateResponseDTO) certificateController.sendCertificate(CERTIFICATE_ID).getEntity();
            verify(sendCertificateFacadeService).sendCertificate(CERTIFICATE_ID);
            assertEquals(IntygServiceResult.OK.toString(), result.getResult());
            assertEquals(CERTIFICATE_ID, result.getCertificateId());
        }
    }

    @Nested
    class GetRelatedCertificate {

        @Test
        void shallReturnRelatedCertificateIdIfExists() {
            final var expectedRelatedCertificateId = "relatedCertificateId";
            doReturn(expectedRelatedCertificateId).when(getRelatedCertificateFacadeService).get(CERTIFICATE_ID);
            final var actualResponse = (GetRelatedCertificateDTO) certificateController.getRelatedCertificate(CERTIFICATE_ID).getEntity();
            assertEquals(expectedRelatedCertificateId, actualResponse.getCertificateId());
        }

        @Test
        void shallReturnNullRelatedCertificateIdIfDoesntExists() {
            final String expectedRelatedCertificateId = null;
            doReturn(expectedRelatedCertificateId).when(getRelatedCertificateFacadeService).get(CERTIFICATE_ID);
            final var actualResponse = (GetRelatedCertificateDTO) certificateController.getRelatedCertificate(CERTIFICATE_ID).getEntity();
            assertEquals(expectedRelatedCertificateId, actualResponse.getCertificateId());
        }
    }

    @Nested
    class GetCertificateUnit {

        @Test
        void shallReturnCorrectDtoMessage() {
            final var expectedDto =
                GetCandidateMessageForCertificateDTO.create("message", "title");
            doReturn(expectedDto).when(getCandidateMesssageForCertificateFacadeService).get(CERTIFICATE_ID);
            final var actualDto = (GetCandidateMessageForCertificateDTO) certificateController.getCandidateMessageForCertificate(
                    CERTIFICATE_ID)
                .getEntity();
            assertEquals(expectedDto.getMessage(), actualDto.getMessage());
        }

        @Test
        void shallReturnCorrectDtoTitle() {
            final var expectedDto =
                GetCandidateMessageForCertificateDTO.create("message", "title");
            doReturn(expectedDto).when(getCandidateMesssageForCertificateFacadeService).get(CERTIFICATE_ID);
            final var actualDto = (GetCandidateMessageForCertificateDTO) certificateController.getCandidateMessageForCertificate(
                    CERTIFICATE_ID)
                .getEntity();
            assertEquals(expectedDto.getTitle(), actualDto.getTitle());
        }
    }

    @Nested
    class CreateCertificateTest {

        @Test
        void shallReturnResponse() throws CreateCertificateException {
            final var expectedDto = new CreateCertificateResponseDTO("certificateId");
            doReturn("certificateId")
                .when(createCertificateFacadeService)
                .create("certificateType", "patientId");
            final var actualDto = (CreateCertificateResponseDTO) certificateController.createCertificate(
                    new CreateCertificateRequestDTO("certificateType", "patientId")
                )
                .getEntity();
            assertEquals(expectedDto, actualDto);
        }
    }
}
