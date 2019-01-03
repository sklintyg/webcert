/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Custom CXF InInterceptor that checks the HTTP status code. If > 399, we log the raw message body and then
 * pass the message on. This is to capture hard-to-track problems caused by consumer systems.
 *
 * @author erikl
 */
public class NotificationInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationInInterceptor.class);

    private static final String RESPONSE_CODE = "org.apache.cxf.message.Message.RESPONSE_CODE";

    /**
     * Constructor that hooks this interceptor into the RECEIVE (e.g. first) phase.
     */
    public NotificationInInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) {

        // We're only checking SoapMessages.
        if (!(message instanceof SoapMessage)) {
            return;
        }

        if (message.containsKey(RESPONSE_CODE)) {
            Object o = message.get(RESPONSE_CODE);
            if (o instanceof Integer) {
                Integer status = (Integer) o;

                // We treat 400 and above as errors.
                if (status >= HttpStatus.BAD_REQUEST.value()) {

                    // Log the raw body "as-is"
                    try {
                        String soapBody = IOUtils.toString(message.getContent(InputStream.class), "UTF-8");
                        LOG.error("Handling HTTP " + status + " error in CertificateStatusUpdateForCareResponse. Raw body is:\n\n"
                                + soapBody);
                        // Re-write response body back into message.
                        ByteArrayInputStream bais = new ByteArrayInputStream(soapBody.getBytes(Charset.forName("UTF-8")));
                        message.setContent(InputStream.class, bais);
                        bais.close();
                    } catch (Exception e) {
                        LOG.error(
                                "Failed to capture body of CertificateStatusUpdateForCareResponse response with non "
                                        + "200 status code, reason: {}",
                                e.getMessage());
                    }
                }
            } else {
                LOG.warn(
                        "Inbound SOAP response contained '{}' property of non-integer type, unable to determine HTTP "
                                + "status code, but treating as OK",
                        RESPONSE_CODE);
            }
        } else {
            LOG.warn("Inbound SOAP response contained no '{}' property, unable to determine HTTP status code, but "
                    + "treating as OK",
                    RESPONSE_CODE);
        }
    }
}
