/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.Assert;


/**
 * Logout filter extending the default SAMLLogoutFilter. The primary use case for this extension is the ability to add the RelayState
 * parameter. This adds functionality to use a redirect URL that is kept even after the redirect/POSTS to the IdP is made.
 *
 * @author Daniel Petersson
 */
public class WebcertSAMLLogoutFilter extends SAMLLogoutFilter {


    public WebcertSAMLLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler[] localHandler,
        LogoutHandler[] globalHandlers) {
        super(logoutSuccessHandler, localHandler, globalHandlers);
    }

    @Override
    public void processLogout(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (requiresLogout(request, response)) {

            try {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth != null && isGlobalLogout(request, auth)) {

                    Assert.isInstanceOf(SAMLCredential.class, auth.getCredentials(),
                        "Authentication object doesn't contain SAML credential, cannot perform global logout");

                    // Terminate the session first
                    for (LogoutHandler handler : globalHandlers) {
                        handler.logout(request, response, auth);
                    }

                    // Notify session participants using SAML Single Logout profile
                    SAMLCredential credential = (SAMLCredential) auth.getCredentials();
                    request.setAttribute(SAMLConstants.LOCAL_ENTITY_ID, credential.getLocalEntityID());
                    request.setAttribute(SAMLConstants.PEER_ENTITY_ID, credential.getRemoteEntityID());
                    SAMLMessageContext context = contextProvider.getLocalAndPeerEntity(request, response);

                    // Set the relaystate so we can redirect the user to the correct logout information
                    String redirectAddress = request.getParameter("RelayState");
                    if (redirectAddress != null) {
                        context.setRelayState(redirectAddress);
                    }
                    profile.sendLogoutRequest(context, credential);
                    samlLogger.log(SAMLConstants.LOGOUT_REQUEST, SAMLConstants.SUCCESS, context);

                } else {

                    super.doFilter(request, response, chain);

                }

            } catch (SAMLException e) {
                logger.debug("Error initializing global logout", e);
                throw new ServletException("Error initializing global logout", e);
            } catch (MetadataProviderException e) {
                logger.debug("Error processing metadata", e);
                throw new ServletException("Error processing metadata", e);
            } catch (MessageEncodingException e) {
                logger.debug("Error encoding outgoing message", e);
                throw new ServletException("Error encoding outgoing message", e);
            }

        } else {

            chain.doFilter(request, response);
        }

    }

}
