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
package se.inera.intyg.webcert.web.web.controller.testability;

import java.io.IOException;
import java.util.Enumeration;

import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;

import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

@Api(value = "testability logMessages", description = "REST API för testbarhet - PDL-loggning")
@Path("/logMessages")
public class LogResource {

    private static final int DEFAULT_TIMEOUT = 1000;

    private long timeOut = DEFAULT_TIMEOUT;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Autowired
    @Qualifier("jmsPDLLogTemplateNoTx")
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogMessages() {
        int count = countMessages();
        long originalTimeout = jmsTemplate.getReceiveTimeout();
        try {
            jmsTemplate.setReceiveTimeout(timeOut);
            for (int i = 0; i < count; i++) {
                jmsTemplate.receive(queue);
            }
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public int countMessages() {
        return jmsTemplate.browse(queue, new BrowserCallback<Integer>() {
            @Override
            public Integer doInJms(Session session, QueueBrowser browser) throws JMSException {
                Enumeration<?> messages = browser.getEnumeration();
                int total = 0;
                while (messages.hasMoreElements()) {
                    messages.nextElement();
                    total++;
                }
                return total;
            }
        });
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public PdlLogMessage getLogMessage() {
        long originalTimeout = jmsTemplate.getReceiveTimeout();
        try {
            jmsTemplate.setReceiveTimeout(timeOut);
            Message message = jmsTemplate.receive(queue);
            String body = ((TextMessage) message).getText();

            return objectMapper.readValue(body, PdlLogMessage.class);
        } catch (JMSException e) {
            throw new RuntimeException("Could not retreive log message: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Could not parse log message: " + e.getMessage(), e);
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
    }
}
