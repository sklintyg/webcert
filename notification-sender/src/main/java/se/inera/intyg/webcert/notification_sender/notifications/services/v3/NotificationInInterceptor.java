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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static se.inera.intyg.webcert.notification_sender.notifications.services.v3.NotificationWSClient.messageContext;

import com.google.common.base.Charsets;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Log the raw body "as-is"
    @Override
    public void handleMessage(Message message) {
        final int status = intValue(message.get(RESPONSE_CODE));
        if (status >= HttpStatus.SC_BAD_REQUEST) {
            try {
                final String payload = IOUtils.toString(message.getContent(InputStream.class), Charsets.UTF_8);

                LOG.error("Handling HTTP error {} in CertificateStatusUpdateForCareResponse: {}, [payload: {}]",
                    status, messageContext(), payload);

                message.setContent(InputStream.class, IOUtils.toInputStream(payload, Charsets.UTF_8));
            } catch (Exception e) {
                LOG.error("Failed to capture body of CertificateStatusUpdateForCareResponse response with non "
                        + "200 status code, reason: {}",
                    e.getMessage());
            }
        } else if (status == -1) {
            LOG.warn("Unable to determine HTTP status code, assuming everything is OK");
        }
    }

    // returns the int value of Object if it's an instance of Integer, otherwise -1
    private int intValue(Object o) {
        return (o instanceof Integer) ? (Integer) o : -1;
    }
}
