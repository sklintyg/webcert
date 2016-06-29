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

package se.inera.intyg.webcert.notification_sender.notifications.routes;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.camel.LoggingLevel;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PartialDateType;

public class NotificationRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRouteBuilder.class);

    /*
     * The second half of this route, sendNotificationToWS, is supposed to be used with redelivery. The
     * route depends on the MQ provider (currently ActiveMQ) for redelivery. Any temporary exception thrown
     * by any component in this route is NOT handled by the route, but triggers a transaction rollback in the
     * MQ provider. The MQ provider will then, if properly configured, put the message back into the queue after
     * the proper redelivery wait time has passed.
     *
     * Any permanent exception is handled by the route, however, and will NOT trigger a redelivery.
     */

    @Override
    public void configure() throws Exception {

        JaxbDataFormat jaxbMessageDataFormatV2 = initializeJaxbMessageDataFormatV2();

        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
                .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
                .transacted()
                .unmarshal("notificationMessageDataFormat")
                .to("bean:notificationTransformer")
                .choice()
                    .when(header(RouteHeaders.VERSION).isEqualTo(SchemaVersion.VERSION_2.name()))
                        .marshal(jaxbMessageDataFormatV2)
                    .otherwise()
                        .marshal("jaxbMessageDataFormat")
                .end()
                .to("sendNotificationWSEndpoint");

        from("sendNotificationWSEndpoint").routeId("sendNotificationToWS")
                .errorHandler(transactionErrorHandler().logExhausted(false))
                .onException(TemporaryException.class).to("direct:temporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
                .transacted()
                .choice()
                    .when(header(RouteHeaders.VERSION).isEqualTo(SchemaVersion.VERSION_2.name()))
                        .unmarshal(jaxbMessageDataFormatV2)
                        .to("bean:notificationWSClientV2")
                    .otherwise()
                        .unmarshal("jaxbMessageDataFormat")
                        .to("bean:notificationWSClient")
                .end();

        from("direct:permanentErrorHandlerEndpoint").routeId("errorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Permanent exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:temporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG, simple("Temporary exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG, simple("Temporary exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}").getText())
                .stop();
    }

    private JaxbDataFormat initializeJaxbMessageDataFormatV2() throws JAXBException {
        // We need to register DatePeriodType with the JAXBContext explicitly for some reason.
        JaxbDataFormat jaxbMessageDataFormatV2 = new JaxbDataFormat(JAXBContext.newInstance(CertificateStatusUpdateForCareType.class, DatePeriodType.class, PartialDateType.class));
        jaxbMessageDataFormatV2.setPartClass("se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType");
        jaxbMessageDataFormatV2.setPartNamespace(new QName("urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:2", "CertificateStatusUpdateForCare"));
        return jaxbMessageDataFormatV2;
    }

}
