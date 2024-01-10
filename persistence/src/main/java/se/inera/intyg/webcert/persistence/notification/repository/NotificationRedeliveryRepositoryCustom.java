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

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Repository
@Transactional
public class NotificationRedeliveryRepositoryCustom {

    private final NotificationRedeliverySQLQueryService notificationRedeliverySQLQueryService;

    @PersistenceContext()
    private EntityManager entityManager;

    public NotificationRedeliveryRepositoryCustom(NotificationRedeliverySQLQueryService notificationRedeliverySQLQueryService) {
        this.notificationRedeliverySQLQueryService = notificationRedeliverySQLQueryService;
    }

    public int sendNotificationsForCertificates(List<String> certificateIds, List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start, LocalDateTime end) {
        performUpdate(notificationRedeliverySQLQueryService.certificates(certificateIds, statuses, start, end));
        return performCount();
    }

    public int sendNotification(String notificationId) {
        performUpdate(notificationRedeliverySQLQueryService.notification(notificationId));
        return performCount();
    }

    private void performUpdate(String sqlUpdate) {
        entityManager.createNativeQuery(sqlUpdate).executeUpdate();
    }

    private int performCount() {
        final var count = (BigInteger) entityManager.createNativeQuery(notificationRedeliverySQLQueryService.count()).getSingleResult();

        return count.intValue();
    }
}
