/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.context.SAMLMessageContext;

public class WebcertSAMLContextProviderLB extends SAMLContextProviderLB {

    @Value("${certificate.view.host.react.client}")
    private String reactClientDomainName;

    @Value("${webcert.domain.name}")
    private String webcertDomainName;

    private static final  String WC2 = "wc2.";
    private static final  String SITHS_WC2_ALIAS = "/siths-wc2";

    @Override
    public SAMLMessageContext getLocalAndPeerEntity(
        HttpServletRequest request, HttpServletResponse response) throws MetadataProviderException {

        final var isRequestFromReactClient  = isRequestFromReactClient(request);
        setServerName(getServerName(isRequestFromReactClient));
        setLocalEntityId(isRequestFromReactClient, request);

        SAMLMessageContext context = new SAMLMessageContext();
        populateGenericContext(request, response, context);
        populateLocalEntityId(context, request.getRequestURI());
        populateLocalContext(context);
        populatePeerEntityId(context);
        populatePeerContext(context);
        return context;
    }

    private boolean isRequestFromReactClient(HttpServletRequest request) {
        final var referrer = request.getHeader("referer");
        return referrer != null && referrer.contains(WC2);
    }

    private String getServerName(boolean isRequestFromReactClient) {
        return isRequestFromReactClient ? reactClientDomainName : webcertDomainName;
    }

    private void setLocalEntityId(boolean isRequestFromReactClient, HttpServletRequest request) {
        if (shouldSetEntityId(request)) {
            final var domainName = isRequestFromReactClient ? reactClientDomainName + SITHS_WC2_ALIAS : webcertDomainName;
            final var entityId = request.getScheme() + "://" + domainName;
            request.setAttribute(SAMLConstants.LOCAL_ENTITY_ID, entityId);
        }
    }

    private boolean shouldSetEntityId(HttpServletRequest request) {
        return request.getAttribute(SAMLConstants.LOCAL_ENTITY_ID) == null && !request.getRequestURI().contains("/alias/");
    }
}
