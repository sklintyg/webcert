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
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PartialDateType;

public class NotificationRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRouteBuilder.class);

    private static final long DEFAULT_TIMEOUT = 60000L;

    @Value("${receiveNotificationForAggregationRequestEndpointUri}")
    private String notificationForAggregationQueue;

    @Value("${receiveNotificationRequestEndpointUri}")
    private String notificationQueue;

    @Value("${notificationSender.batchTimeout}")
    private Long batchAggregationTimeout = DEFAULT_TIMEOUT;

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

        // Start for aggregation route. All notifications enter this route. Draft saved and signed for non fk7263
        // goes into an aggregation state where we once per minute perform filtering so only the newest ANDRAD per intygsId
        // OR a SIGNERAD are forwarded to the 'receiveNotificationRequestEndpoint' queue. The others are discarded.
        // Do note that the above only applies to non-fk7263 ANDRAD and SIGNERAD, all others will be forwarded directly.

        from(notificationForAggregationQueue).routeId("aggregateNotification")
                .onException(Exception.class).to("direct:temporaryErrorHandlerEndpoint").end()
                .transacted()
                .log(LoggingLevel.INFO, LOG, simple("ENTER - route: aggregateNotification: Header: ${header[handelse]}").getText())
                .removeHeader(Constants.JMSX_GROUP_ID)
                .removeHeader(Constants.JMSX_GROUP_SEQ)
        .choice()
                .when(header(NotificationRouteHeaders.INTYGS_TYP).isEqualTo(Fk7263EntryPoint.MODULE_ID))
                    .to(notificationQueue)
                .when(directRoutingPredicate())
                    .to(notificationQueue)
                .otherwise()
                    .aggregate(new GroupedExchangeAggregationStrategy())
                    .constant(true)
                    .completionInterval(batchAggregationTimeout)
                    .forceCompletionOnStop()
                    .to("bean:notificationAggregator")
                    .split(body())
                    .to(notificationQueue).end()
                .end();

        // All routes below relate to pre WC 5.0 notification sending, e.g. all that enters 'receiveNotificationRequestEndpoint'
        // should have normal resend semantics etc.
        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
                .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
                .transacted()
                .unmarshal("notificationMessageDataFormat")
                .to("bean:notificationTransformer")
                .choice()
                    .when(header(NotificationRouteHeaders.VERSION).isEqualTo(SchemaVersion.VERSION_2.name()))
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
                    .when(header(NotificationRouteHeaders.VERSION).isEqualTo(SchemaVersion.VERSION_2.name()))
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

    /*
     * Returns true if the handelse header is NOT of type ANDRAT and not SIGNAT.
     * I.e. all except those two types shall be directly routed to the 'receiveNotificationRequestEndpoint' aka
     * notificationQueue without any aggregation or filtering.
     */
    private Predicate directRoutingPredicate() {
        return PredicateBuilder
                .and(
                    header(NotificationRouteHeaders.HANDELSE).isNotEqualTo(HandelsekodEnum.ANDRAT.value()),
                    header(NotificationRouteHeaders.HANDELSE).isNotEqualTo(HandelsekodEnum.SIGNAT.value()));
    }

    private JaxbDataFormat initializeJaxbMessageDataFormatV2() throws JAXBException {
        // We need to register DatePeriodType with the JAXBContext explicitly for some reason.
        JaxbDataFormat jaxbMessageDataFormatV2 = new JaxbDataFormat(JAXBContext.newInstance(CertificateStatusUpdateForCareType.class, DatePeriodType.class, PartialDateType.class));
        jaxbMessageDataFormatV2.setPartClass("se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType");
        jaxbMessageDataFormatV2.setPartNamespace(new QName("urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:2", "CertificateStatusUpdateForCare"));
        return jaxbMessageDataFormatV2;
    }

}
