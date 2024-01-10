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

package se.inera.intyg.webcert.persistence.notification.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Service
public class NotificationRedeliverySQLQueryService {

    public String count() {
        return "SELECT COUNT(*) FROM NOTIFICATION_REDELIVERY;";
    }

    public String certificates(List<String> certificateIds, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end) {
        final var certificateIdsQuery = getListFilteringQuery(certificateIds);
        final var certificateIdFilteringQuery = "H.INTYGS_ID in " + certificateIdsQuery;
        final var eventTableQuery = getEventTableQuery(certificateIdFilteringQuery, statuses, start, end);

        return "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) "
            + eventTableQuery + ";";
    }

    public String notification(String notificationId) {
        return "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME) "
            + "VALUES(" + notificationId + ", 'STANDARD', now());";
    }

    private static String getListFilteringQuery(List<String> values) {
        final var formattedValues = values.stream()
            .map(s -> "'" + s + "'")
            .collect(Collectors.joining(","));
        return "(" + formattedValues + ")";
    }

    private String getEventTableQuery(String uniqueFiltering, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end) {
        final var statusFilteringQuery = getStatusFilteringQuery(statuses);
        final var timePeriodFilteringQuery = getTimePeriodFiltering(start, end);
        final var completeFilteringQuery = getFilteringQuery(uniqueFiltering, statusFilteringQuery, timePeriodFilteringQuery);
        final var joinQuery = statuses == null || statuses.isEmpty() ? "" : "INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID ";

        if (completeFilteringQuery.isEmpty()) {
            return "";
        }

        return "SELECT H.ID, 'STANDARD', now() FROM HANDELSE H "
            + joinQuery
            + completeFilteringQuery
            + " ORDER BY H.TIMESTAMP";
    }

    private String getFilteringQuery(String... s) {
        if (s.length == 0) {
            return "";
        }
        return "WHERE " + Arrays.stream(s).filter(string -> !string.isEmpty()).collect(Collectors.joining(" AND "));
    }

    private String getTimePeriodFiltering(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return "";
        }

        final var endString = end != null ? "'" + end + "'" : "now()";

        return "H.TIMESTAMP BETWEEN '" + start + "' AND " + endString;
    }

    private String getStatusFilteringQuery(List<NotificationDeliveryStatusEnum> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "";
        }

        final var formattedStatuses = getListFilteringQuery(
            statuses.stream()
                .map(NotificationDeliveryStatusEnum::value)
                .collect(Collectors.toList())
        );

        return "HM.DELIVERY_STATUS in " + formattedStatuses;
    }
}
