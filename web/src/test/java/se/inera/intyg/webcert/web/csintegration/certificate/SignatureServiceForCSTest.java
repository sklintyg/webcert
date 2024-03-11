/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateXmlRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@ExtendWith(MockitoExtension.class)
class SignatureServiceForCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TICKET_ID = "ticketId";
    private static final String CERTIFICATE_XML_DATA = "certificateXmlData";
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    private CreateSignatureTicketService createSignatureTicketService;

    @InjectMocks
    private SignatureServiceForCS signatureServiceForCS;


    @Nested
    class StartSigningProcess {

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signatureServiceForCS.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID, false));
        }

        @Test
        void shallReturnSignaturBiljett() {
            final var expectedTicket = new SignaturBiljett();
            final var certificateXmlRequestDTO = GetCertificateXmlRequestDTO.builder().build();
            doReturn(certificateXmlRequestDTO).when(csIntegrationRequestFactory).getCertificateXmlRequest();
            doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            doReturn(CERTIFICATE_XML_DATA).when(csIntegrationService).getCertificateXml(certificateXmlRequestDTO, CERTIFICATE_ID);
            doReturn(expectedTicket).when(createSignatureTicketService)
                .create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID, false, CERTIFICATE_XML_DATA);

            final var actualTicket = signatureServiceForCS.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE,
                TICKET_ID, false);
            assertEquals(expectedTicket, actualTicket);
        }
    }

    @Nested
    class FakeSignature {

        @Test
        void shallReturnNullIfCertificateDontExistsInCertificateService() {
            doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
            assertNull(signatureServiceForCS.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, TICKET_ID));
        }
    }
}
