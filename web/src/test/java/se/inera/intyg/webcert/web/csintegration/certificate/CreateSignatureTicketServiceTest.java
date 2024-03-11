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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;

@ExtendWith(MockitoExtension.class)
class CreateSignatureTicketServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TICKET_ID = "ticketId";
    private static final String CERTIFICATE_XML = "certificateXml";
    @Mock
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @InjectMocks
    private CreateSignatureTicketService createSignatureTicketService;

    @Test
    void shallReturnNullIfSignMethodIsNotSupported() {
        assertNull(createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.NETID_PLUGIN, TICKET_ID, false,
            CERTIFICATE_XML));
    }

    @Test
    void shallThrowIfTicketFromServiceIsNull() {
        doReturn(null).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID, false,
                CERTIFICATE_XML);

        assertThrows(IllegalStateException.class,
            () -> createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID, false,
                CERTIFICATE_XML));
    }

    @Test
    void shallReturnCreatedSignatureTicket() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(expectedTicket).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID, false,
                CERTIFICATE_XML);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            false,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }
}
