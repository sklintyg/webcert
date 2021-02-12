/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.web.service.notification.NotificationRedeliveryJobService;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);

    @Autowired
    private NotificationRedeliveryService notificationRedeliveryService;

    @Autowired
    private NotificationRedeliveryJobService notificationRedeliveryJobService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT59S";
    private static final String LOCK_AT_LEAST = "PT55S";

    @Scheduled(cron = "${job.notification.redelivery.cron:-}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running notification redelivery job...");

        notificationRedeliveryJobService.resendNotifications();

        /*final List<NotificationRedelivery> redeliveryList = notificationRedeliveryService.getNotificationsForRedelivery();
        redeliveryList.sort(Comparator.comparing(NotificationRedelivery::getEventId));

        // TODO Add handling of delays in the queues to make sure events don't get redelivered twice.
        // TODO Add handling of job not finished when scheduled to run next time.

        for (NotificationRedelivery redelivery : redeliveryList) {

            try {
                final Handelse event = notificationRedeliveryService.getEventById(redelivery.getEventId());

                if (notificationRedeliveryService.isRedundantRedelivery(event))  {
                    notificationRedeliveryService.discardRedundantRedelivery(event, redelivery);
                } else if (redelivery.getCorrelationId() == null) {
                    notificationRedeliveryJobService.createManualNotification(event, redelivery);
                } else {
                    final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                        NotificationRedeliveryMessage.class);

                    final CertificateStatusUpdateForCareType statusUpdate = redeliveryMessage.getV3();
                    notificationRedeliveryJobService.completeStatusUpdate(statusUpdate, redeliveryMessage, event);

                    final String statusUpdateXml = notificationRedeliveryJobService.marshal(statusUpdate);
                    notificationRedeliveryJobService.sendJmsMessage(statusUpdateXml, event, redelivery);
                }

            // TODO Sort out these exception with regard to resend or fail, and which action to perform.
            } catch (NoSuchElementException e) { //when no handelse exists
                LOG.error(notificationRedeliveryJobService
                    .getLogInfoString(redelivery) + "Could not find a corresponding event in table Handelse.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (IOException | ModuleException | ModuleNotFoundException e) {
                LOG.error(notificationRedeliveryJobService
                    .getLogInfoString(redelivery) + "Error setting a certificate on status update object.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (WebCertServiceException e) {
                LOG.error(e.getMessage(), e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            } catch (Exception e) {
                LOG.error(notificationRedeliveryJobService
                    .getLogInfoString(redelivery) + "An exception occurred.", e);
                //notificationRedeliveryService.setNotificationFailure(redelivery.getEventId(), redelivery.getCorrelationId());
            }
        }*/
    }
}
