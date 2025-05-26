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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ConcurrentModificationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlResponseDTO;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpSignatureServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;

@ExtendWith(MockitoExtension.class)
class SignServiceForCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TICKET_ID = "ticketId";
    private static final String CERTIFICATE_XML_DATA = "certificateXmlData";
    private static final byte[] SIGNATURE_IN_BYTE = "signature".getBytes(StandardCharsets.UTF_8);
    private static final String SIGN_CERTIFICATE = "certificate";
    private static final String USER_IP_ADDRESS = "userIpAddress";
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    private FinalizeCertificateSignService finalizeCertificateSignService;

    @Mock
    private CreateSignatureTicketService createSignatureTicketService;
    @Mock
    private FakeSignatureServiceCS fakeSignatureServiceCS;
    @Mock
    private XmlUnderskriftServiceImpl xmlUnderskriftService;
    @Mock
    private GrpSignatureServiceImpl grpUnderskriftService;
    @Mock
    private RedisTicketTracker redisTicketTracker;

    @InjectMocks
    private SignServiceForCS signServiceForCS;


    @Nested
    class StartSigningProcess {

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signServiceForCS.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID, USER_IP_ADDRESS));
        }

        @Test
        void shallThrowExceptionIfVersionDontMatch() {
            final var certificateXmlRequestDTO = GetCertificateXmlRequestDTO.builder().build();
            doReturn(certificateXmlRequestDTO).when(csIntegrationRequestFactory).getCertificateXmlRequest();
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(GetCertificateXmlResponseDTO.builder()
                .xml(CERTIFICATE_XML_DATA)
                .version(1L)
                .build()
            ).when(csIntegrationService).getCertificateXml(certificateXmlRequestDTO, CERTIFICATE_ID);
            assertThrows(ConcurrentModificationException.class,
                () -> signServiceForCS.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE,
                    TICKET_ID, USER_IP_ADDRESS));
        }

        @Test
        void shallReturnSignaturBiljett() {
            final var expectedTicket = new SignaturBiljett();
            final var certificateXmlRequestDTO = GetCertificateXmlRequestDTO.builder().build();
            doReturn(certificateXmlRequestDTO).when(csIntegrationRequestFactory).getCertificateXmlRequest();
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(
                GetCertificateXmlResponseDTO.builder()
                    .xml(CERTIFICATE_XML_DATA)
                    .build()
            ).when(csIntegrationService).getCertificateXml(certificateXmlRequestDTO, CERTIFICATE_ID);
            doReturn(expectedTicket).when(createSignatureTicketService)
                .create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID, USER_IP_ADDRESS,
                    new String(Base64.getDecoder().decode(CERTIFICATE_XML_DATA), StandardCharsets.UTF_8));

            final var actualTicket = signServiceForCS.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE,
                TICKET_ID, USER_IP_ADDRESS);
            assertEquals(expectedTicket, actualTicket);
        }
    }

    @Nested
    class FakeSignature {

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signServiceForCS.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, TICKET_ID));
        }

        @Test
        void shallFinalizeSignWithCertificateFromCertificateService() {
            final var ticket = new SignaturBiljett();
            final var certificate = new Certificate();
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(ticket)
                .certificate(certificate)
                .build();

            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(fakeSignatureServiceCS)
                .finalizeFakeSignature(TICKET_ID);

            signServiceForCS.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, TICKET_ID);
            verify(finalizeCertificateSignService).finalizeSign(certificate);
        }

        @Test
        void shallReturnSignaturBiljett() {
            final var expectedTicket = new SignaturBiljett();
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(expectedTicket)
                .build();

            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(fakeSignatureServiceCS)
                .finalizeFakeSignature(TICKET_ID);

            final var actualTicket = signServiceForCS.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, TICKET_ID);
            assertEquals(expectedTicket, actualTicket);
        }
    }

    @Nested
    class NetIdSignature {

        @Test
        void shallThrowIfTicketFromRedisTrackerIsNull() {
            doReturn(null).when(redisTicketTracker).findBiljett(TICKET_ID);
            assertThrows(WebCertServiceException.class,
                () -> signServiceForCS.netidSignature(TICKET_ID, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE)
            );
        }

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            final var ticket = new SignaturBiljett();
            ticket.setIntygsId(CERTIFICATE_ID);
            doReturn(ticket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signServiceForCS.netidSignature(TICKET_ID, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE));
        }

        @Test
        void shallFinalizeSignWithCertificateFromCertificateService() {
            final var certificate = new Certificate();
            final var expectedTicket = new SignaturBiljett();
            expectedTicket.setIntygsId(CERTIFICATE_ID);
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(expectedTicket)
                .certificate(certificate)
                .build();

            doReturn(expectedTicket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(xmlUnderskriftService)
                .finalizeSignatureForCS(expectedTicket, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE);

            signServiceForCS.netidSignature(TICKET_ID, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE);
            verify(finalizeCertificateSignService).finalizeSign(certificate);
        }

        @Test
        void shallReturnSignaturBiljett() {
            final var expectedTicket = new SignaturBiljett();
            expectedTicket.setIntygsId(CERTIFICATE_ID);
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(expectedTicket)
                .build();

            doReturn(expectedTicket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(xmlUnderskriftService)
                .finalizeSignatureForCS(expectedTicket, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE);

            final var actualTicket = signServiceForCS.netidSignature(TICKET_ID, SIGNATURE_IN_BYTE, SIGN_CERTIFICATE);
            assertEquals(expectedTicket, actualTicket);
        }
    }

    @Nested
    class GrpSignature {

        @Test
        void shallThrowIfTicketFromRedisTrackerIsNull() {
            doReturn(null).when(redisTicketTracker).findBiljett(TICKET_ID);
            assertThrows(WebCertServiceException.class,
                () -> signServiceForCS.grpSignature(TICKET_ID, SIGNATURE_IN_BYTE)
            );
        }

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            final var ticket = new SignaturBiljett();
            ticket.setIntygsId(CERTIFICATE_ID);
            doReturn(ticket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signServiceForCS.grpSignature(TICKET_ID, SIGNATURE_IN_BYTE));
        }

        @Test
        void shallFinalizeSignWithCertificateFromCertificateService() {
            final var certificate = new Certificate();
            final var expectedTicket = new SignaturBiljett();
            expectedTicket.setIntygsId(CERTIFICATE_ID);
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(expectedTicket)
                .certificate(certificate)
                .build();

            doReturn(expectedTicket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(grpUnderskriftService)
                .finalizeSignatureForCS(expectedTicket, SIGNATURE_IN_BYTE, null);

            signServiceForCS.grpSignature(TICKET_ID, SIGNATURE_IN_BYTE);
            verify(finalizeCertificateSignService).finalizeSign(certificate);
        }

        @Test
        void shallReturnSignaturBiljett() {
            final var expectedTicket = new SignaturBiljett();
            expectedTicket.setIntygsId(CERTIFICATE_ID);
            final var expectedResponse = FinalizedCertificateSignature.builder()
                .signaturBiljett(expectedTicket)
                .build();

            doReturn(expectedTicket).when(redisTicketTracker).findBiljett(TICKET_ID);
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(expectedResponse).when(grpUnderskriftService)
                .finalizeSignatureForCS(expectedTicket, SIGNATURE_IN_BYTE, null);

            final var actualTicket = signServiceForCS.grpSignature(TICKET_ID, SIGNATURE_IN_BYTE);
            assertEquals(expectedTicket, actualTicket);
        }
    }

    @Nested
    class SigneringsStatus {

        @Test
        void shallThrowIfTicketIsNull() {
            doReturn(null).when(redisTicketTracker).findBiljett(TICKET_ID);
            assertThrows(WebCertServiceException.class, () ->
                signServiceForCS.signeringsStatus(TICKET_ID));
        }

        @Test
        void shallReturnTicketFromRedisTracker() {
            final var expectedTicket = new SignaturBiljett();
            doReturn(expectedTicket).when(redisTicketTracker).findBiljett(TICKET_ID);

            final var actualTicket = signServiceForCS.signeringsStatus(TICKET_ID);
            assertEquals(expectedTicket, actualTicket);
        }
    }
}
