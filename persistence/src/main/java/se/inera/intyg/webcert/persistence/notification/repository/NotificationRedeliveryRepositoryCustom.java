/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.notification.repository;

import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ACTIVATION_TIME;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.END;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ID;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.START;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.STATUS;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Repository
@Transactional
public class NotificationRedeliveryRepositoryCustom {

    private final NotificationRedeliverySQLQueryGenerator notificationRedeliverySQLQueryGenerator;

    @PersistenceContext()
    private EntityManager entityManager;

    public NotificationRedeliveryRepositoryCustom(NotificationRedeliverySQLQueryGenerator notificationRedeliverySQLQueryGenerator) {
        this.notificationRedeliverySQLQueryGenerator = notificationRedeliverySQLQueryGenerator;
    }

    public int sendNotificationsForCertificates(List<String> certificateIds, List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start, LocalDateTime end, LocalDateTime activationTime) {
        final var sql = notificationRedeliverySQLQueryGenerator.certificates(statuses, start, end, activationTime);
        final var query = entityManager.createQuery(sql);
        setParameters(certificateIds, statuses, start, end, activationTime, query);
        query.executeUpdate();
        return performCount();
    }

    public int sendNotificationsForUnits(List<String> unitIds, List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start, LocalDateTime end, LocalDateTime activationTime) {
        final var sql = notificationRedeliverySQLQueryGenerator.units(statuses, start, end, activationTime);
        final var query = entityManager.createQuery(sql);
        setParameters(unitIds, statuses, start, end, activationTime, query);
        query.executeUpdate();
        return performCount();
    }

    public int sendNotificationsForCareGiver(String careGiverId, List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start, LocalDateTime end, LocalDateTime activationTime) {
        final var id = careGiverId + "-%";
        final var sql = notificationRedeliverySQLQueryGenerator.careGiver(statuses, start, end, activationTime);
        final var query = entityManager.createQuery(sql);
        setParameters(id, statuses, start, end, activationTime, query);
        query.executeUpdate();
        return performCount();
    }

    public int sendNotificationsForTimePeriod(List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start, LocalDateTime end,
        LocalDateTime activationTime) {
        final var sql = notificationRedeliverySQLQueryGenerator.timePeriod(statuses, start, end, activationTime);
        final var query = entityManager.createQuery(sql);
        setParameters(statuses, start, end, activationTime, query);
        query.executeUpdate();
        return performCount();
    }

    public int sendNotification(String notificationId) {
        final var sql = notificationRedeliverySQLQueryGenerator.notification();
        final var query = entityManager.createQuery(sql);
        query.setParameter(ID, notificationId);
        query.executeUpdate();
        return performCount();
    }

    private void setParameters(String id, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime, Query query) {
        query.setParameter(ID, id);
        setParameters(statuses, start, end, activationTime, query);
    }

    private void setParameters(List<String> ids, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime, Query query) {
        query.setParameter(ID, ids);
        setParameters(statuses, start, end, activationTime, query);
    }

    private static void setParameters(
        List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime activationTime, Query query) {
        query.setParameter(STATUS, statuses);

        if (start != null) {
            query.setParameter(START, start);
        }

        if (end != null) {
            query.setParameter(END, start);
        }

        if (activationTime != null) {
            query.setParameter(ACTIVATION_TIME, activationTime);
        }
    }

    private int performCount() {
        final var count = (BigInteger) entityManager.createNativeQuery(notificationRedeliverySQLQueryGenerator.count()).getSingleResult();

        return count.intValue();
    }
}
