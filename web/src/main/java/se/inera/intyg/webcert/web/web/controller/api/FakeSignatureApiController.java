/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Transactional
@Path("/signature")
@Profile("!prod")
public class FakeSignatureApiController extends AbstractApiController {

    protected static final String UTF_8 = "UTF-8";

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    @Autowired
    private UnderskriftService underskriftService;

    /**
     * Signera utkast. Endast fejkinloggning.
     *
     * FLYTTA TILL EGEN BEAN som är !prod annoterad!!!!
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/fejksignera/{ticketId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public SignaturStateDTO fejkSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, @PathParam("ticketId") String ticketId, @Context HttpServletRequest request) {

        // Start by doing an extra server-side check of FAKE authentication.
        WebCertUser user = getWebCertUserService().getUser();
        if (user.getAuthenticationMethod() != AuthenticationMethod.FAKE) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "Fake signing is only allowed for users logged in by FAKE AuthenticationMethod.");
        }
        verifyIsAuthorizedToSignIntyg(intygsTyp);

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        SignaturBiljett sb = underskriftService.fakeSignature(intygsId, intygsTyp, version, ticketId);

        return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
                .withId(sb.getTicketId())
                .withIntygsId(sb.getIntygsId())
                .withStatus(sb.getStatus())
                .withVersion(sb.getVersion())
                .withHash(sb.getHash()) // This is what you stuff into NetiD SIGN.
                .build();
    }

    private void verifyIsAuthorizedToSignIntyg(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG)
                .orThrow();
    }
}
