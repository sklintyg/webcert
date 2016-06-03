/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;

/**
 * Created by eriklupander on 2016-05-09.
 */
public class WebcertWebSSOProfileImpl extends org.springframework.security.saml.websso.WebSSOProfileImpl {
    /**
     * Returns AuthnRequest SAML message to be used to demand authentication from an IDP described using
     * idpEntityDescriptor, with an expected response to the assertionConsumer address.
     *
     * This overridden version explicitly sets the attributeConsumingServiceIndex for better control over IdP behaviour.
     *
     * @param context           message context
     * @param options           preferences of message creation
     * @param assertionConsumer assertion consumer where the IDP should respond
     * @param bindingService    service used to deliver the request
     * @return authnRequest ready to be sent to IDP
     * @throws SAMLException             error creating the message
     * @throws MetadataProviderException error retreiving metadata
     */
    @Override
    protected AuthnRequest getAuthnRequest(SAMLMessageContext context, WebSSOProfileOptions options,
                                           AssertionConsumerService assertionConsumer,
                                           SingleSignOnService bindingService) throws SAMLException, MetadataProviderException {

        AuthnRequest authnRequest = super.getAuthnRequest(context, options, assertionConsumer, bindingService);

        // Only specify attributeConsumingServiceIndex for SITHS-based authentications.
        if (options.getAuthnContexts().contains(AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT)) {
            authnRequest.setAttributeConsumingServiceIndex(1);
        }

        return authnRequest;
    }
}
