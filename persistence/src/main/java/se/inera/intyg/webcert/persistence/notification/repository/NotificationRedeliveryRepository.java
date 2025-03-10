/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

public interface NotificationRedeliveryRepository extends
    JpaRepository<NotificationRedelivery, String> {


    Optional<NotificationRedelivery> findByCorrelationId(String correlationId);

    List<NotificationRedelivery> findByCorrelationIdNull();

    Optional<NotificationRedelivery> findByEventId(Long handelseId);

    List<NotificationRedelivery> findByRedeliveryTimeLessThan(LocalDateTime currentTime);

    List<NotificationRedelivery> findByRedeliveryTimeLessThan(LocalDateTime currentTime,
        Pageable pageable);

    default List<NotificationRedelivery> findRedeliveryUpForDelivery(LocalDateTime time, int limit) {
        final var pageZero = 0;
        final var pageable = PageRequest.of(
            pageZero,
            limit,
            Sort.by(Order.asc("redeliveryTime"))
        );
        return findByRedeliveryTimeLessThan(time, pageable);
    }

    List<NotificationRedelivery> findByRedeliveryTime(LocalDateTime currentTime);

    List<NotificationRedelivery> findByAttemptedDeliveries(Integer attemptedDeliveries);

    @Modifying
    @Query("Update NotificationRedelivery n SET n.redeliveryTime=null WHERE n.eventId in (:ids)")
    void clearRedeliveryTime(@Param("ids") List<Long> ids);

    @Query("select nr from NotificationRedelivery nr where nr.eventId in :eventIds")
    List<NotificationRedelivery> getRedeliveriesByEventIds(@Param("eventIds") List<Long> eventIds);

    default int eraseRedeliveriesForEventIds(List<Long> eventIds) {
        final var redeliveries = getRedeliveriesByEventIds(eventIds);
        deleteAll(redeliveries);
        return redeliveries.size();
    }

    @Modifying
    @Query(value = """
        INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME)
        SELECT h.ID, 'STANDARD', now() FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.INTYGS_ID IN :certificateIds AND hm.DELIVERY_STATUS IN :statuses
        ORDER BY h.TIMESTAMP""", nativeQuery = true)
    int sendNotificationsForCertificates(@Param("certificateIds") List<String> certificateIds,
        @Param("statuses") List<String> statuses);

    @Modifying
    @Query(value = """
        INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME)
        SELECT h.ID, 'STANDARD', :activationTime FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.ENHETS_ID IN :unitIds AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end ORDER BY h.TIMESTAMP""", nativeQuery = true)
    int sendNotificationsForUnits(@Param("unitIds") List<String> unitIds,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("activationTime") LocalDateTime activationTime);

    @Modifying
    @Query(value = """
        INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME)
        SELECT h.ID, 'STANDARD', :activationTime FROM HANDELSE h
        INNER JOIN HANDELSE_METADATA hm ON h.ID = hm.HANDELSE_ID
        WHERE h.VARDGIVAR_ID = :careGiverId AND hm.DELIVERY_STATUS IN :statuses
        AND h.TIMESTAMP BETWEEN :start AND :end ORDER BY h.TIMESTAMP""", nativeQuery = true)
    int sendNotificationsForCareGiver(@Param("careGiverId") String careGiverId,
        @Param("statuses") List<String> statuses,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("activationTime") LocalDateTime activationTime);

    @Modifying
    @Query(value = """
        INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) \
        SELECT h.ID, 'STANDARD', now() FROM HANDELSE h WHERE h.ID = :notificationId""", nativeQuery = true)
    int sendNotification(@Param("notificationId") String notificationId);

}
