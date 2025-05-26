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

package se.inera.intyg.webcert.web.service.underskrift.grp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpOrderRequest;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpSubjectIdentifier;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

@ExtendWith(MockitoExtension.class)
class GrpRestServiceTest {

    @Mock
    private RestClient grpRestClient;
    @Mock
    private SignaturBiljett ticket;
    @Mock
    private RedisTicketTracker redisTicketTracker;
    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @InjectMocks
    private GrpRestService grpRestService;

    private static final String INIT_PATH = "/init";
    private static final String COLLECT_PATH = "/collect";
    private static final String SERVICE_ID = "serviceId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REF_ID = "refId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PROVIDER = "provider";
    private static final String PERSONID_TYPE = "TIN";
    private static final String REQUEST_TYPE = "requestType";
    private static final String END_USER_INFO = "endUserInfo";
    private static final String PROVIDER_BANKID = "bankid";
    private static final String REQUEST_TYPE_AUTH = "AUTH";
    private static final String USER_IP_ADDRESS = "userIpAddress";
    private static final String PERSON_ID = "personId";
    private static final String BASE_URL = "grpBaseUrl";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(grpRestService, SERVICE_ID, SERVICE_ID);
        ReflectionTestUtils.setField(grpRestService, DISPLAY_NAME, DISPLAY_NAME);
        ReflectionTestUtils.setField(grpRestService, ACCESS_TOKEN, ACCESS_TOKEN);
        ReflectionTestUtils.setField(grpRestService, BASE_URL, "https://baseUrl.test");
    }

    @Nested
    class GrpRestServiceInit {

        private RestClient.RequestBodyUriSpec uriSpec;

        @BeforeEach
        void setUp() {
            final var responseSpec = mock(RestClient.ResponseSpec.class);
            uriSpec = mock(RestClient.RequestBodyUriSpec.class);
            when(grpRestClient.post()).thenReturn(uriSpec);
            when(uriSpec.uri(any(URI.class))).thenReturn(uriSpec);
            when(uriSpec.header(any(), any())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(uriSpec.body(any(GrpOrderRequest.class))).thenReturn(uriSpec);
            when(ticket.getTicketId()).thenReturn(TRANSACTION_ID);
            when(ticket.getUserIpAddress()).thenReturn(USER_IP_ADDRESS);
        }

        @Test
        void shouldCallGrpInitWithExpectedQueryParameters() {
            grpRestService.init(PERSON_ID, ticket);
            verify(uriSpec).uri(uriCaptor.capture());

            final var query = uriCaptor.getValue().getQuery();
            assertAll(
                () -> assertTrue(query.contains(String.join("=", SERVICE_ID, SERVICE_ID))),
                () -> assertTrue(query.contains(String.join("=", DISPLAY_NAME, DISPLAY_NAME))),
                () -> assertTrue(query.contains(String.join("=", PROVIDER, PROVIDER_BANKID))),
                () -> assertTrue(query.contains(String.join("=", REQUEST_TYPE, REQUEST_TYPE_AUTH))),
                () -> assertTrue(query.contains(String.join("=", TRANSACTION_ID, TRANSACTION_ID))),
                () -> assertTrue(query.contains(String.join("=", END_USER_INFO, USER_IP_ADDRESS)))
            );
        }

        @Test
        void shouldCallGrpInitWithExpectedUrl() {
            grpRestService.init(PERSON_ID, ticket);
            verify(uriSpec).uri(uriCaptor.capture());
            assertAll(
                () -> assertEquals("https", uriCaptor.getValue().getScheme()),
                () -> assertEquals(INIT_PATH, uriCaptor.getValue().getPath()),
                () -> assertEquals("baseUrl.test", uriCaptor.getValue().getHost())
            );
        }

        @Test
        void shouldCallGrpInitWithExpectedHeader() {
            grpRestService.init(PERSON_ID, ticket);
            verify(uriSpec).header(ACCESS_TOKEN, ACCESS_TOKEN);
        }

        @Test
        void shouldCallGrpInitWithExpectedBody() {
            final var expectedBody = GrpOrderRequest.builder()
                .subjectIdentifier(GrpSubjectIdentifier.builder()
                    .value(PERSON_ID)
                    .type(PERSONID_TYPE)
                    .build())
                .build();
            grpRestService.init(PERSON_ID, ticket);
            verify(uriSpec).body(expectedBody);
        }

        @Test
        void shouldThrowWebcertServiceExceptionWhenGrpRequestFailure() {
            when(ticket.getIntygsId()).thenReturn("certificatId");
            when(uriSpec.retrieve()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
            assertThrows(WebCertServiceException.class, () -> grpRestService.init(PERSON_ID, ticket));
        }
    }

    @Nested
    class GrpRestServiceCollect {

        @SuppressWarnings("rawtypes")
        private RestClient.RequestHeadersUriSpec uriSpec;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
            final var responseSpec = mock(RestClient.ResponseSpec.class);
            uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            when(grpRestClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(any(URI.class))).thenReturn(uriSpec);
            when(uriSpec.header(any(), any())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shouldCallGrpCollectWithExpectedQueryParameters() {
            grpRestService.collect(REF_ID, TRANSACTION_ID);
            verify(uriSpec).uri(uriCaptor.capture());

            final var query = uriCaptor.getValue().getQuery();
            assertAll(
                () -> assertTrue(query.contains(String.join("=", SERVICE_ID, SERVICE_ID))),
                () -> assertTrue(query.contains(String.join("=", REF_ID, REF_ID))),
                () -> assertTrue(query.contains(String.join("=", TRANSACTION_ID, TRANSACTION_ID)))
            );
        }

        @Test
        void shouldCallGrpCollectWithExpectedUrl() {
            grpRestService.collect(REF_ID, TRANSACTION_ID);
            verify(uriSpec).uri(uriCaptor.capture());
            assertAll(
                () -> assertEquals("https", uriCaptor.getValue().getScheme()),
                () -> assertEquals(COLLECT_PATH, uriCaptor.getValue().getPath()),
                () -> assertEquals("baseUrl.test", uriCaptor.getValue().getHost())
            );
        }

        @Test
        void shouldCallGrpInitWithExpectedHeader() {
            grpRestService.collect(REF_ID, TRANSACTION_ID);
            verify(uriSpec).header(ACCESS_TOKEN, ACCESS_TOKEN);
        }

        @Test
        void shouldReturnNullWhenGrpRequestFailure() {
            when(redisTicketTracker.findBiljett(TRANSACTION_ID)).thenReturn(ticket);
            when(ticket.getIntygsId()).thenReturn("certificatId");
            when(uriSpec.retrieve()).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
            assertNull(grpRestService.collect(REF_ID, TRANSACTION_ID));
        }
    }
}
