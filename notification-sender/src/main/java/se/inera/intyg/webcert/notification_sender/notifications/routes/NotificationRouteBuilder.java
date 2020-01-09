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
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.DiscardCandidateException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;

public class NotificationRouteBuilder extends SpringRouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRouteBuilder.class);

    private static final long DEFAULT_TIMEOUT = 60000L;
    private static final int DEFAULT_MAXREDELIVERIES = 5;

    @Value("${receiveNotificationForAggregationRequestEndpointUri}")
    private String notificationForAggregationQueue;

    @Value("${receiveNotificationRequestEndpointUri}")
    private String notificationQueue;

    @Value("${notificationSender.batchTimeout}")
    private Long batchAggregationTimeout = DEFAULT_TIMEOUT;

    @Value("${notificationSender.maximumRedeliveries}")
    private int maximumRedeliveries = DEFAULT_MAXREDELIVERIES;

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
    public void configure() throws JAXBException {
        JaxbDataFormat jaxbMessageDataFormatV3 = initializeJaxbMessageDataFormatV3();

        // Start for aggregation route. All notifications enter this route. Draft saved and signed for non fk7263
        // goes into an aggregation state where we once per minute perform filtering so only the newest ANDRAD per
        // intygsId
        // forwarded to the 'receiveNotificationRequestEndpoint' queue. The others are discarded.
        // Do note that the above only applies to non-fk7263 ANDRAD, all others will be forwarded directly.

        from(notificationForAggregationQueue).routeId("aggregateNotification")
            .onException(Exception.class).to("direct:temporaryErrorHandlerEndpoint").end()
            .transacted("txTemplate")
            .choice()
            .when(header(NotificationRouteHeaders.INTYGS_TYP).isEqualTo(Fk7263EntryPoint.MODULE_ID))
            .to(notificationQueue)
            .when(directRoutingPredicate())
            .to(notificationQueue)
            .otherwise()
            .wireTap("direct:signatWireTap")
            .aggregate(new GroupedExchangeAggregationStrategy())
            .constant(true)
            .completionInterval(batchAggregationTimeout)
            .forceCompletionOnStop()
            .to("bean:notificationAggregator")
            .split(body())
            .to(notificationQueue).end()
            .end();

        // The wiretap is used to directly forward SIGNAT messages (see INTYG-2744) to the send queue while the original
        // SIGNAT is passed on into the aggregation phase. The aggregation phase never emits any SIGNAT, only ANDRAT.
        from("direct:signatWireTap")
            .choice()
            .when(header(NotificationRouteHeaders.HANDELSE).isEqualTo(HandelsekodEnum.SIGNAT.value()))
            .to(notificationQueue)
            .end();

        // All routes below relate to pre WC 5.0 notification sending, e.g. all that enters
        // 'receiveNotificationRequestEndpoint'
        // should have normal resend semantics etc. Reads from the notificationQueue.
        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
            .onException(TemporaryException.class).to("direct:temporaryErrorHandlerEndpoint").end()
            .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
            .transacted("txTemplate")
            .unmarshal("notificationMessageDataFormat")
            .to("bean:notificationTransformer")
            .marshal(jaxbMessageDataFormatV3)
            .to("sendNotificationWSEndpoint");

        from("sendNotificationWSEndpoint").routeId("sendNotificationToWS")
            .errorHandler(transactionErrorHandler().logExhausted(false))
            .onException(TemporaryException.class).to("direct:temporaryErrorHandlerEndpoint").end()
            .onException(DiscardCandidateException.class)
            .handled(isTimeToDiscard()).to("direct:discardCandidateErrorHandlerEndpoint").end()
            .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
            .transacted("txTemplate")
            .unmarshal(jaxbMessageDataFormatV3)
            .to("bean:notificationWSClientV3");

        from("direct:permanentErrorHandlerEndpoint").routeId("errorLogging")
            .log(LoggingLevel.ERROR, LOG,
                simple("Permanent exception for intygs-id: ${header[intygsId]}, to: ${header[logiskAdress]}"
                    + ", with message: ${exception.message}\n ${exception.stacktrace}")
                    .getText())
            .stop();

        from("direct:discardCandidateErrorHandlerEndpoint").routeId("discardCandidateErrorLogging")
            .choice()
            .when(isTimeToDiscard())
            .log(LoggingLevel.WARN, LOG,
                simple("Throwing away notification (COSMIC typ B) after trying to deliver ${header[handelse]} "
                    + "notification ${header[JMSXDeliveryCount]} times for intygs-id: ${header[intygsId]}"
                    + ", to: ${header[logiskAdress]}").getText())
            .when(header("JMSRedelivered").isEqualTo(false))
            .log(LoggingLevel.INFO, LOG,
                simple("Caught error for ${header[handelse]} notification (total delivery count 1) "
                    + "of COSMIC typ B for intygs-id: ${header[intygsId]}, to: ${header[logiskAdress]}")
                    .getText())
            .otherwise()
            .log(LoggingLevel.INFO, LOG,
                simple("Caught error for ${header[handelse]} notification (total delivery count "
                    + "${header[JMSXDeliveryCount]}) of COSMIC typ B for intygs-id: ${header[intygsId]}"
                    + ", to: ${header[logiskAdress]}").getText())
            .stop();

        from("direct:temporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
            .choice()
            .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
            .log(LoggingLevel.ERROR, LOG,
                simple("Temporary exception for intygs-id: ${header[intygsId]}, to: ${header[logiskAdress]}, "
                    + "with message: ${exception.message}\n ${exception.stacktrace}")
                    .getText())
            .otherwise()
            .log(LoggingLevel.WARN, LOG,
                simple("Temporary exception for intygs-id: ${header[intygsId]}, to: ${header[logiskAdress]}, "
                    + "with message: ${exception.message}").getText())
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

    private Predicate isTimeToDiscard(final int limit) {
        if (limit == -1) {
            return PredicateBuilder.constant(false);
        } else if (limit == 0) {
            return PredicateBuilder.constant(true);
        } else {
            return PredicateBuilder.and(
                header("JMSRedelivered").isEqualTo(true),
                header("JMSXDeliveryCount").isGreaterThan(limit));
        }
    }

    private Predicate isTimeToDiscard() {
        // Using property instead of trying to get the configured maximumRedeliveries from the ConnectionFactory
        return isTimeToDiscard(maximumRedeliveries);
    }

    // CHECKSTYLE:OFF LineLength
    private JaxbDataFormat initializeJaxbMessageDataFormatV3() throws JAXBException {
        // We need to register DatePeriodType with the JAXBContext explicitly for some reason.
        JaxbDataFormat jaxbMessageDataFormatV3 = new JaxbDataFormat(
            JAXBContext.newInstance(CertificateStatusUpdateForCareType.class, DatePeriodType.class, PartialDateType.class,
                XPathType.class, PQType.class));
        jaxbMessageDataFormatV3.setPartClass(
            "se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType");
        jaxbMessageDataFormatV3
            .setPartNamespace(new QName("urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3",
                "CertificateStatusUpdateForCare"));
        return jaxbMessageDataFormatV3;
    }
    // CHECKSTYLE:ON LineLength

}
