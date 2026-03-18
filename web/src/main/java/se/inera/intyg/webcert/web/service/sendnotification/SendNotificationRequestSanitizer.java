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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CountNotificationsForUnitsRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCareGiverRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForCertificatesRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

public class SendNotificationRequestSanitizer {

  private SendNotificationRequestSanitizer() {
    throw new IllegalStateException("Utility class");
  }

  public static String sanitize(String value) {
    return StringUtils.deleteWhitespace(value);
  }

  public static List<String> sanitize(List<String> values) {
    return removeBlankSpaces(values);
  }

  public static SendNotificationsForCertificatesRequestDTO sanitize(
      SendNotificationsForCertificatesRequestDTO request) {
    return SendNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(removeBlankSpaces(request.getCertificateIds()))
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .build();
  }

  public static SendNotificationsForUnitsRequestDTO sanitize(
      SendNotificationsForUnitsRequestDTO request) {
    return SendNotificationsForUnitsRequestDTO.builder()
        .unitIds(removeBlankSpaces(request.getUnitIds()))
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .start(request.getStart())
        .end(request.getEnd())
        .activationTime(getActivationTime(request.getActivationTime()))
        .build();
  }

  public static SendNotificationsForCareGiverRequestDTO sanitize(
      SendNotificationsForCareGiverRequestDTO request) {
    return SendNotificationsForCareGiverRequestDTO.builder()
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .start(request.getStart())
        .end(request.getEnd())
        .activationTime(getActivationTime(request.getActivationTime()))
        .build();
  }

  public static CountNotificationsForCareGiverRequestDTO sanitize(
      CountNotificationsForCareGiverRequestDTO request) {
    return CountNotificationsForCareGiverRequestDTO.builder()
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .start(request.getStart())
        .end(request.getEnd())
        .activationTime(getActivationTime(request.getActivationTime()))
        .build();
  }

  public static CountNotificationsForUnitsRequestDTO sanitize(
      CountNotificationsForUnitsRequestDTO request) {
    return CountNotificationsForUnitsRequestDTO.builder()
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .end(request.getEnd())
        .start(request.getStart())
        .activationTime(getActivationTime(request.getActivationTime()))
        .build();
  }

  public static CountNotificationsForCertificatesRequestDTO sanitize(
      CountNotificationsForCertificatesRequestDTO request) {
    return CountNotificationsForCertificatesRequestDTO.builder()
        .certificateIds(removeBlankSpaces(request.getCertificateIds()))
        .statuses(removeIncorrectStatuses(request.getStatuses()))
        .activationTime(getActivationTime(request.getActivationTime()))
        .build();
  }

  private static List<String> removeBlankSpaces(List<String> values) {
    if (values == null) {
      return Collections.emptyList();
    }
    return values.stream().map(StringUtils::deleteWhitespace).toList();
  }

  public static List<NotificationDeliveryStatusEnum> removeIncorrectStatuses(
      List<NotificationDeliveryStatusEnum> statuses) {
    return statuses.stream()
        .filter(
            status ->
                status == NotificationDeliveryStatusEnum.SUCCESS
                    || status == NotificationDeliveryStatusEnum.FAILURE)
        .toList();
  }

  private static LocalDateTime getActivationTime(LocalDateTime activationTime) {
    return activationTime == null ? LocalDateTime.now() : activationTime;
  }

  public static List<String> getStatusesAsString(List<NotificationDeliveryStatusEnum> statuses) {
    return statuses.stream().map(NotificationDeliveryStatusEnum::value).toList();
  }
}
