/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

@Service
public class NotificationRedeliveryJobServiceImpl implements NotificationRedeliveryJobService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJobServiceImpl.class);

    @Autowired
    private NotificationRedeliveryService notificationRedeliveryService;

    @Autowired
    private NotificationRedeliveryStatusUpdateCreatorService notificationRedeliveryStatusUpdateCreatorService;

    @Autowired
    private HandelseRepository eventRepository;

    @Override
    @PerformanceLogging(eventAction = "job-resend-scheduled-notifications", eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
        eventCategory = MdcLogConstants.EVENT_CATEGORY_PROCESS)
    public void resendScheduledNotifications(int batchSize) {
        final var startTimeInMilliseconds = System.currentTimeMillis();
        final var notificationsToResend = notificationRedeliveryService.getNotificationsForRedelivery(batchSize);
        final var successfullySent = resend(notificationsToResend);
        final var endTimeInMilliseconds = System.currentTimeMillis();

        LOG.debug("Processed {} notification for redelivery in {} seconds. Number of failures: {}.",
            total(notificationsToResend),
            durationInSeconds(startTimeInMilliseconds, endTimeInMilliseconds),
            noOfFailed(notificationsToResend, successfullySent));
    }

    private int resend(List<NotificationRedelivery> notificationRedeliveryList) {
        var successfullySent = 0;

        final var eventMap = getEventMap(notificationRedeliveryList);

        for (NotificationRedelivery notificationRedelivery : notificationRedeliveryList) {
            final var event = eventMap.get(notificationRedelivery.getEventId());
            final var success = resend(notificationRedelivery, event);
            if (success) {
                successfullySent++;
            }
        }

        return successfullySent;
    }

    private Map<Long, Handelse> getEventMap(List<NotificationRedelivery> notificationRedeliveryList) {
        final var eventIds = notificationRedeliveryList.stream()
            .map(NotificationRedelivery::getEventId)
            .collect(Collectors.toList());

        final var events = eventRepository.findAllById(eventIds);

        final var eventMap = new HashMap<Long, Handelse>(events.size());
        events.stream().forEach(event -> eventMap.put(event.getId(), event));

        return eventMap;
    }

    private boolean resend(NotificationRedelivery notificationRedelivery, Handelse event) {
        try {
            final var statusUpdateXml = getCertificateStatusUpdateXmlABytes(notificationRedelivery, event);
            notificationRedeliveryService.resend(notificationRedelivery, event, statusUpdateXml);
            return true;
        } catch (Exception e) {
            LOG.error(getLogInfoString(notificationRedelivery) + "An exception occurred.", e);
            notificationRedeliveryService.handleErrors(notificationRedelivery, event, e);
            return false;
        }
    }

    private String getLogInfoString(NotificationRedelivery redelivery) {
        return String.format("Failure resending message [notificationId: %s, correlationId: %s]. ", redelivery.getEventId(),
            redelivery.getCorrelationId());
    }

    private byte[] getCertificateStatusUpdateXmlABytes(NotificationRedelivery notificationRedelivery, Handelse event)
        throws ModuleNotFoundException, TemporaryException, ModuleException, IOException, JAXBException {
        final var statusUpdateXml = notificationRedeliveryStatusUpdateCreatorService
            .getCertificateStatusUpdateXml(notificationRedelivery, event);
        return statusUpdateXml.getBytes(StandardCharsets.UTF_8);
    }


    private int noOfFailed(List<NotificationRedelivery> notificationsToResend, int successfullySent) {
        return total(notificationsToResend) - successfullySent;
    }

    private int total(List<NotificationRedelivery> notificationsToResend) {
        return notificationsToResend.size();
    }

    private long durationInSeconds(long startTimeInMilliseconds, long endTimeInMilliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds(endTimeInMilliseconds - startTimeInMilliseconds);
    }
}
