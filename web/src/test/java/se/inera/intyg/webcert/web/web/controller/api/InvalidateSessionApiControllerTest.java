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
package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.launchid.InvalidateSessionService;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

@ExtendWith(MockitoExtension.class)
public class InvalidateSessionApiControllerTest {

    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String USER_HSA_ID = "TSTNMT2321000156-1079";
    private final InvalidateSessionService invalidateSessionService = mock(InvalidateSessionService.class);
    private InvalidateRequest invalidateRequest;
    @InjectMocks
    private InvalidateSessionApiController controller;

    @Test
    public void assertThatControllerRunsWhenGivenCorrectValues() {
        invalidateRequest = getInvalidateRequest();
        Response response = controller.invalidateSession(invalidateRequest);
        verify(invalidateSessionService).invalidateSessionIfActive(any());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void assertThatControllerRunsWhenGivenWrongValues() {
        invalidateRequest = getInvalidateRequest();
        invalidateRequest.setLaunchId(null);

        Response response = controller.invalidateSession(invalidateRequest);
        verify(invalidateSessionService, never()).invalidateSessionIfActive(any());

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldStillReturnNoContentAfterThrowingException() {
        invalidateRequest = getInvalidateRequest();
        Response response = controller.invalidateSession(invalidateRequest);

        doThrow(new NullPointerException()).when(invalidateSessionService).invalidateSessionIfActive(invalidateRequest);

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }


    @Nested
    class InvalidateRequestValidation {

        @Test
        public void validateRequestIfCorrectFormat() {
            invalidateRequest = getInvalidateRequest();
            Response response = controller.invalidateSession(invalidateRequest);

            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        @Test
        public void invalidateRequestWithWrongValuesShouldReturnBadRequest() {
            invalidateRequest = getInvalidateRequest();
            invalidateRequest.setUserHsaId(null);
            Response response = controller.invalidateSession(invalidateRequest);

            assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        @Test
        public void invalidateRequestWithLaunchIdSetToNull() {
            invalidateRequest = getInvalidateRequest();
            invalidateRequest.setLaunchId(null);
            invalidateRequest.setUserHsaId(USER_HSA_ID);

            Response response = controller.invalidateSession(invalidateRequest);

            assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        @Test
        public void invalidateRequestWithHsaIdSetToNull() {
            invalidateRequest = getInvalidateRequest();
            invalidateRequest.setLaunchId(LAUNCH_ID);
            invalidateRequest.setUserHsaId(null);

            Response response = controller.invalidateSession(invalidateRequest);

            assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }
    }

    private InvalidateRequest getInvalidateRequest() {
        InvalidateRequest dto = new InvalidateRequest();
        dto.setLaunchId(LAUNCH_ID);
        dto.setUserHsaId(USER_HSA_ID);
        return dto;
    }
}
