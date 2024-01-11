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

import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ACTIVATION_TIME;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.END;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.ID;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.START;
import static se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliverySQLConstants.STATUS;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Component
public class NotificationRedeliverySQLQueryGenerator {

    private static final String INSERT_QUERY = "INSERT INTO NOTIFICATION_REDELIVERY (HANDELSE_ID, REDELIVERY_STRATEGY, REDELIVERY_TIME)";
    private static final String SPACE = " ";
    private static final String END_QUERY = ";";
    private static final String ID_PREFIX = ":";

    public String count() {
        return "SELECT COUNT(*) FROM NOTIFICATION_REDELIVERY;";
    }

    public String notification() {
        return INSERT_QUERY + SPACE + "VALUES(" + ID_PREFIX + ID + ", 'STANDARD', now())" + END_QUERY;
    }

    public String certificates(List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime) {
        final var prefix = "H.INTYGS_ID in";
        return getQueryForFilteringOnIds(prefix, statuses, start, end, activationTime);
    }

    public String units(List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start, LocalDateTime end,
        LocalDateTime activationTime) {
        final var prefix = "H.ENHETS_ID in";
        return getQueryForFilteringOnIds(prefix, statuses, start, end, activationTime);
    }

    public String careGiver(List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime) {
        final var filteringOnCareProvider = "H.ENHETS_ID LIKE" + SPACE + ID_PREFIX + ID;
        return getQueryWithFiltering(filteringOnCareProvider, statuses, start, end, activationTime);
    }

    public String timePeriod(List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime) {
        return getQueryWithFiltering(null, statuses, start, end, activationTime);
    }

    private String getQueryForFilteringOnIds(String idsFilteringPrefix, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime) {
        final var certificateIdFilteringQuery = idsFilteringPrefix + SPACE + ID_PREFIX + ID;
        return getQueryWithFiltering(certificateIdFilteringQuery, statuses, start, end, activationTime);
    }

    private String getQueryWithFiltering(
        String uniqueFiltering,
        List<NotificationDeliveryStatusEnum> statuses,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime activationTime
    ) {
        final var eventTableQuery = getEventTableQuery(uniqueFiltering, statuses, start, end, activationTime);
        return INSERT_QUERY + SPACE + eventTableQuery + END_QUERY;
    }

    private String getEventTableQuery(String uniqueFiltering, List<NotificationDeliveryStatusEnum> statuses, LocalDateTime start,
        LocalDateTime end, LocalDateTime activationTime) {
        final var statusFilteringQuery = getStatusFilteringQuery(statuses);
        final var timePeriodFilteringQuery = getTimePeriodFiltering(start, end);
        final var completeFilteringQuery = getFilteringQuery(uniqueFiltering, statusFilteringQuery, timePeriodFilteringQuery);

        final var joinQuery =
            statuses == null || statuses.isEmpty() ? "" : "INNER JOIN HANDELSE_METADATA HM ON H.ID = HM.HANDELSE_ID" + SPACE;
        final var activationTimeQuery = activationTime != null ? ID_PREFIX + ACTIVATION_TIME : "now()";

        if (completeFilteringQuery.isEmpty()) {
            return "";
        }

        return "SELECT H.ID, 'STANDARD',"
            + SPACE + activationTimeQuery
            + SPACE + "FROM HANDELSE H"
            + SPACE + joinQuery
            + completeFilteringQuery
            + SPACE + "ORDER BY H.TIMESTAMP";
    }

    private String getFilteringQuery(String... s) {
        if (s.length == 0) {
            return "";
        }
        return "WHERE" + SPACE + Arrays.stream(s)
            .filter(string -> string != null && !string.isEmpty())
            .collect(Collectors.joining(" AND "));
    }

    private String getTimePeriodFiltering(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return "";
        }

        final var endString = end != null ? ID_PREFIX + END : "now()";
        return "H.TIMESTAMP BETWEEN " + ID_PREFIX + START + " AND " + endString;
    }

    private String getStatusFilteringQuery(List<NotificationDeliveryStatusEnum> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "";
        }

        return "HM.DELIVERY_STATUS in" + SPACE + ID_PREFIX + STATUS;
    }
}
