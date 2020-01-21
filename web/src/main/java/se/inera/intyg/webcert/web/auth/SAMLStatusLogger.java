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

import org.opensaml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

public class SAMLStatusLogger extends SAMLDefaultLogger {
    private final static Logger log = LoggerFactory.getLogger(SAMLStatusLogger.class);

    @Autowired
    private MonitoringLogService logService;

    @Override
    public void log(String operation, String result, SAMLMessageContext context) {
        log(operation, result, context, SecurityContextHolder.getContext().getAuthentication(), null);
    }

    @Override
    public void log(String operation, String result, SAMLMessageContext context, Exception e) {
        log(operation, result, context, SecurityContextHolder.getContext().getAuthentication(), e);
    }

    @Override
    public void log(String operation, String result, SAMLMessageContext context, Authentication a, Exception e) {

        if (!log.isWarnEnabled()) return;

        if (context.getInboundSAMLMessage() != null) {
            SAMLObject samlObj = context.getInboundSAMLMessage();
            Element responseObj = samlObj.getDOM();

            Node issuerNode = responseObj.getFirstChild();
            String issuer = issuerNode.getFirstChild().getNodeValue();
            Node signatureNode = issuerNode.getNextSibling();
            Node statusNode = signatureNode.getNextSibling();
            Node statusCodeNode = statusNode.getFirstChild();
            Node statusMessageNode = statusCodeNode.getNextSibling();
            String status = statusMessageNode.getFirstChild().getNodeValue();

            logService.logSamlStatusForFailedLogin(issuer, status);
        }

        super.log(operation, result, context, a, e);
    }
}
