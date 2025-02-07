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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CreateSignatureTicketServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TICKET_ID = "ticketId";
    private static final String CERTIFICATE_XML = "certificateXml";
    private static final String PERSON_ID = "personId";
    @Mock
    private XmlUnderskriftServiceImpl xmlUnderskriftService;
    @Mock
    private GrpUnderskriftServiceImpl grpUnderskriftService;
    @Mock
    private WebCertUserService webCertUserService;

    private WebCertUser user;
    @InjectMocks
    private CreateSignatureTicketService createSignatureTicketService;

    @BeforeEach
    void setUp() {
        user = mock(WebCertUser.class);
        doReturn(user).when(webCertUserService).getUser();
    }

    @Test
    void shallThrowIfTicketFromServiceIsNull() {
        doReturn(AuthenticationMethod.FAKE).when(user).getAuthenticationMethod();
        doReturn(null).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                CERTIFICATE_XML);

        assertThrows(IllegalStateException.class,
            () -> createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
                CERTIFICATE_XML));
    }

    @Test
    void shallReturnCreatedSignatureTicketForUserWithAuthenticationMethodFake() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(AuthenticationMethod.FAKE).when(user).getAuthenticationMethod();
        doReturn(expectedTicket).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                CERTIFICATE_XML);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }

    @Test
    void shallReturnCreatedSignatureTicketForUserWithAuthenticationMethodSiths() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(AuthenticationMethod.SITHS).when(user).getAuthenticationMethod();
        doReturn(expectedTicket).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                CERTIFICATE_XML);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }

    @Test
    void shallReturnCreatedSignatureTicketForUserWithAuthenticationMethodNetId() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(AuthenticationMethod.NET_ID).when(user).getAuthenticationMethod();
        doReturn(expectedTicket).when(xmlUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                CERTIFICATE_XML);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }

    @Test
    void shallReturnCreatedSignatureTicketForUserWithAuthenticationMethodBankId() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(AuthenticationMethod.BANK_ID).when(user).getAuthenticationMethod();
        doReturn(expectedTicket).when(grpUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                null);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }

    @Test
    void shallReturnCreatedSignatureTicketForUserWithAuthenticationMethodMobiltBankID() {
        final var expectedTicket = new SignaturBiljett();
        doReturn(AuthenticationMethod.MOBILT_BANK_ID).when(user).getAuthenticationMethod();
        doReturn(expectedTicket).when(grpUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.FAKE, TICKET_ID,
                null);

        final var actualTicket = createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.FAKE, TICKET_ID,
            CERTIFICATE_XML);

        assertEquals(expectedTicket, actualTicket);
    }

    @Test
    void shallStartGrpCollectPollerIfSignmethodGRP() {
        final var expectedTicket = new SignaturBiljett();
        expectedTicket.setSignMethod(SignMethod.GRP);
        doReturn(AuthenticationMethod.MOBILT_BANK_ID).when(user).getAuthenticationMethod();
        doReturn(PERSON_ID).when(user).getPersonId();
        doReturn(expectedTicket).when(grpUnderskriftService)
            .skapaSigneringsBiljettMedDigest(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, Optional.empty(), SignMethod.GRP, TICKET_ID,
                null);

        createSignatureTicketService.create(CERTIFICATE_ID, CERTIFICATE_TYPE, 0L, SignMethod.GRP, TICKET_ID,
            CERTIFICATE_XML);

        verify(grpUnderskriftService).startGrpCollectPoller(PERSON_ID, expectedTicket);
    }
}
