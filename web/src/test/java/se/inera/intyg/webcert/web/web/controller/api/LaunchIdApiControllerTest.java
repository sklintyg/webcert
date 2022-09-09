/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.web.web.controller.api.dto.InvalidateRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LaunchIdApiControllerTest {

    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String USER_HSA_ID = "TSTNMT2321000156-1079";
    @InjectMocks
    LaunchIdApiController controller;

    @Test
    public void testIfInvalidateSessionReceivesDesiredDto() {
        InvalidateRequest dto = getInvalidateRequest();

        Response response = controller.invalidateSession(dto);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void invalidateRequestWithWrongValuesShouldReturnBadRequest() {
        InvalidateRequest dto = new InvalidateRequest();

        Response response = controller.invalidateSession(dto);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void invalidateRequestWithNullValuesShouldReturnTrue() {
        InvalidateRequest invalidateRequest = new InvalidateRequest();
        invalidateRequest.setLaunchId(null);
        invalidateRequest.setUserHsaId(USER_HSA_ID);

        assertTrue(invalidateRequest.formatIsWrong());

        invalidateRequest.setLaunchId(LAUNCH_ID);
        invalidateRequest.setUserHsaId(null);

        assertTrue(invalidateRequest.formatIsWrong());

        invalidateRequest.setLaunchId(LAUNCH_ID);
        invalidateRequest.setUserHsaId(USER_HSA_ID);

        assertFalse(invalidateRequest.formatIsWrong());
    }

    private InvalidateRequest getInvalidateRequest() {
        InvalidateRequest dto = new InvalidateRequest();
        dto.setLaunchId(LAUNCH_ID);
        dto.setUserHsaId(USER_HSA_ID);
        return dto;
    }
}
