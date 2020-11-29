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

package se.inera.intyg.webcert.notification_sender.notifications.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@Component
public class NotificationRedeliveryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJob.class);
    private static final String JOB_NAME = "NotificationRedeliveryJob.run";
    private static final String LOCK_AT_MOST = "PT2M";
    private static final String LOCK_AT_LEAST = "PT1M";

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("jmsTemplateNotificationWSSender")
    private JmsTemplate jmsTemplate;


    @Scheduled(cron = "${job.notification.resend.cron}")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Running job for notificaition redelivery...");

        List<NotificationRedelivery> notificationList = notificationRedeliveryRepository.findByRedeliveryTimeLessThan(LocalDateTime.now());

        for (NotificationRedelivery notification : notificationList) {
            Handelse event = handelseRepository.findById(notification.getEventId()).orElse(null);
            String statusUpdateMessageXml = notification.getMessage();

            try {
                jmsTemplate.convertAndSend(statusUpdateMessageXml, message -> {
                    message.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, notification.getCorrelationId());
                    message.setStringProperty(NotificationRouteHeaders.INTYGS_ID, event.getIntygsId());
                    message.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, event.getEnhetsId());
                    message.setStringProperty(NotificationRouteHeaders.USER_ID, event.getHanteratAv());
                    message.setLongProperty(Constants.JMS_TIMESTAMP, Instant.now().getEpochSecond());
                    return message;
                });
            } catch (JmsException e) {
                LOG.error("Exception occurred resending notification: " + e.getMessage());
            }
            LOG.info("Initiated redelivery of {} notifications", notificationList.size());
        }
    }
}
