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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
//import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationResend;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationResendRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Component
public class NotificationResendJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResendJob.class);
    private static final String JOB_NAME = "NotificationResendJob.run";
    private static final String LOCK_AT_MOST = "PT30S";
    private static final String LOCK_AT_LEAST = "PT5S";

    @Autowired
    private HandelseRepository handelseRepository;

    @Autowired
    private NotificationResendRepository notificationResendRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    @Qualifier("jmsNotificationTemplateForAggregation")
    private JmsTemplate jmsTemplate;


    //@Value("${job.notification.resend.after.min}")
    //private int resendAfterMin;


    @Scheduled(cron = "${job.notification.resend.cron}")
    //@SchedulerLock(name = JOB_NAME, lockAtLeastFor = LOCK_AT_LEAST, lockAtMostFor = LOCK_AT_MOST)
    public void run() {
        LOG.info("Starting job to resend notifications...");

        LocalDate today = LocalDate.now();
        List<NotificationResend> notificationList = notificationResendRepository.findByResendTimeLessThan(LocalDateTime.now());

        for (NotificationResend notification : notificationList) {

            Handelse event = handelseRepository.findById(notification.getEventId()).orElse(null);
            String eventAsJson = eventToJson(event);

            try {
                jmsTemplate.convertAndSend(eventAsJson, message -> {
                    message.setStringProperty("Kenny", "Rogers");
                    return message;
                });
            } catch (JmsException e) {
                LOG.error("Exception occurred resending notification");
            }
        }
        LOG.info("Initiated redelivery of {} notifications", notificationList.size());
    }


    private String eventToJson(Handelse event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            LOG.error("Problem occured when trying to create and marshall NotificationMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }
}
