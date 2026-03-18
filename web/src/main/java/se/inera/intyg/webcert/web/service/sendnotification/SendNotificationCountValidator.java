/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.sendnotification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@Component
@RequiredArgsConstructor
public class SendNotificationCountValidator {

  private final HandelseRepository handelseRepository;

  @Value("${max.allowed.notification.send}")
  private int maxAllowedNotificationSend;

  public void careGiver(String careGiverId, SendNotificationsForCareGiverRequestDTO request) {
    final var insertsForCareGiver =
        handelseRepository.countInsertsForCareGiver(
            careGiverId,
            getStatusesAsString(request.getStatuses()),
            request.getStart(),
            request.getEnd());
    if (insertsForCareGiver > maxAllowedNotificationSend) {
      throw new IllegalArgumentException(buildErrorMessage(insertsForCareGiver));
    }
  }

  private static List<String> getStatusesAsString(List<NotificationDeliveryStatusEnum> statuses) {
    return statuses.stream().map(NotificationDeliveryStatusEnum::value).toList();
  }

  public void certificates(SendNotificationsForCertificatesRequestDTO request) {
    final var insertsForCertificates =
        handelseRepository.countInsertsForCertificates(
            request.getCertificateIds(), getStatusesAsString(request.getStatuses()));
    if (insertsForCertificates > maxAllowedNotificationSend) {
      throw new IllegalArgumentException(buildErrorMessage(insertsForCertificates));
    }
  }

  public void units(SendNotificationsForUnitsRequestDTO request) {
    final var insertsForUnits =
        handelseRepository.countInsertsForUnits(
            request.getUnitIds(),
            getStatusesAsString(request.getStatuses()),
            request.getStart(),
            request.getEnd());
    if (insertsForUnits > maxAllowedNotificationSend) {
      throw new IllegalArgumentException(buildErrorMessage(insertsForUnits));
    }
  }

  public void notification(String notificationId) {
    final var insertsForNotification = handelseRepository.countNotification(notificationId);
    if (insertsForNotification > maxAllowedNotificationSend) {
      throw new IllegalArgumentException(buildErrorMessage(insertsForNotification));
    }
  }

  private String buildErrorMessage(int numberOfInserts) {
    return "Request exceeded maximum number of notifications allowed to be sent. Number of inserts '%s' exceeds the limit of '%s'"
        .formatted(numberOfInserts, maxAllowedNotificationSend);
  }
}
