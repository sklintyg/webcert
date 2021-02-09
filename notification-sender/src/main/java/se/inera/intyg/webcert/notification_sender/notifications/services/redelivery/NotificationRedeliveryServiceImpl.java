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

package se.inera.intyg.webcert.notification_sender.notifications.services.redelivery;

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.CLIENT;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.DISCARD;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;

import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;


@Service
public class NotificationRedeliveryServiceImpl implements NotificationRedeliveryService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryServiceImpl.class);

    @Autowired
    private HandelseRepository handelseRepo;
    @Autowired
    private NotificationRedeliveryRepository notificationRedeliveryRepo;

    @Override
    public List<NotificationRedelivery> getNotificationsForRedelivery() {
        // TODO: Get all notifications up for redelivery. Remove redeliveries that should be discarded.
        // TODO: Set correlation-id if missing.
        return notificationRedeliveryRepo.findByRedeliveryTimeLessThan(LocalDateTime.now());
    }

    @Transactional
    @Override
    public boolean isRedundantRedelivery(Handelse event) {
        return checkRedundantRedelivery(event);
    }

    @Transactional
    @Override
    public void discardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        doDiscardRedundantRedelivery(event, redelivery);
    }

    @Transactional
    @Override
    public Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    @Transactional
    @Override
    public void initiateManualNotification(NotificationRedelivery redelivery, Handelse event) {
        handelseRepo.save(event);
        notificationRedeliveryRepo.save(redelivery);
    }

    @Transactional
    @Override
    public void setSentWithV3Client(Handelse event, NotificationRedelivery redelivery) {
        // TODO: Shall this have a different status? Why?
        event.setDeliveryStatus(CLIENT);
        handelseRepo.save(event);
        deleteNotificationRedelivery(redelivery);
    }

    private void deleteNotificationRedelivery(NotificationRedelivery record) {
        notificationRedeliveryRepo.delete(record);
    }

    private boolean checkRedundantRedelivery(Handelse event) {
        long numberOfSignedEvents = 0;
        HandelsekodEnum code = event.getCode();
        if (code == ANDRAT || code == SKAPAT) {
            String certificateId = event.getIntygsId();
            List<Handelse> events = handelseRepo.findByIntygsId(certificateId);
            numberOfSignedEvents = events.stream()
                .filter(e -> (code == ANDRAT && e.getCode() == SIGNAT && e.getDeliveryStatus() == SUCCESS)
                    ||  (e.getCode() == HandelsekodEnum.RADERA)).count();
        }
        return numberOfSignedEvents > 0;
    }

    private void doDiscardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery) {
        deleteNotificationRedelivery(redelivery);
        event.setDeliveryStatus(DISCARD);
        handelseRepo.save(event);
        LOG.info("Aborting redelivery attempts of redundant notification [eventId: {}, correlationId: {}, eventCode: {}, "
                + "certificateId: {}, logicalAddress: {}]. The event has been removed.", event.getId(), redelivery.getCorrelationId(),
            event.getCode(), event.getIntygsId(), event.getEnhetsId());
    }
}
