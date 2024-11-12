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

package se.inera.intyg.webcert.web.web.controller.testability.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.inera.intyg.webcert.web.service.testability.FakeLoginService;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.FakeLoginDTO;

@RequiredArgsConstructor
@Slf4j
public class FakeLoginTestabilityController {

    private final FakeLoginService fakeLoginService;
    private final ObjectMapper objectMapper;

    @POST
    @Path(value = "/fake")
    @Consumes(MediaType.APPLICATION_JSON)
    public void login(@Context HttpServletRequest request, FakeLoginDTO fakeLoginDTO) {
        fakeLoginService.login(fakeLoginDTO, request);
    }

    @POST
    @Path("/logout")
    public void logout(@Context HttpServletRequest request) {
        fakeLoginService.logout(request.getSession(false));
    }
}
