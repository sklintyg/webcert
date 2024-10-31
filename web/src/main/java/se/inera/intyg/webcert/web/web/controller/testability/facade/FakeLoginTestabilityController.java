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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.web.service.testability.FakeLoginService;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.FakeLoginDTO;

@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class FakeLoginTestabilityController {

    private final FakeLoginService fakeLoginService;
    private final ObjectMapper objectMapper;

    @Path(value = "/fake")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response login(@FormParam("userJsonDisplay") String loginObject, @Context HttpServletRequest request)
        throws JsonProcessingException {
//        @FormParam("enhetId") String enhetId,
//        @FormParam("sekretessMarkerad") String sekretessMarkerad,
//        @FormParam("forNamn") String forNamn,
//        @FormParam("efterNamn") String efterNamn,
//        @FormParam("origin") String origin,
//        @FormParam("authenticationMethod") String authenticationMethod,
//        @FormParam("legitimeradeYrkesgrupper") List<String> legitimeradeYrkesgrupper) {

        final var fakeLoginDTO = objectMapper.readValue(loginObject, FakeLoginDTO.class);
        fakeLoginService.login(fakeLoginDTO, request);
        return Response.ok().build();
    }

    @Path("/logout")
    public void logout(@Context HttpServletRequest request) {
        fakeLoginService.logout(request.getSession(false));
    }

}
