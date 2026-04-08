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
package se.inera.intyg.webcert.notificationstub;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStoreV3;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStubStateBean;
import se.inera.intyg.webcert.notificationstub.v3.stat.NotificationStubEntry;
import se.inera.intyg.webcert.notificationstub.v3.stat.StatTransformerUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

// CHECKSTYLE:OFF LineLength
@RestController
@Profile("dev")
@RequestMapping("/api/notification-api")
public class NotificationStubRestApi {

  @Autowired private NotificationStoreV3 notificationStoreV3;

  @Autowired private NotificationStubStateBean stubStateBean;

  @GetMapping(value = "/notifieringar/v3", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<CertificateStatusUpdateForCareType> notifieringarV3() {
    return notificationStoreV3.getNotifications();
  }

  @GetMapping(value = "/notifieringar/v3/stats", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> notifieringarV3Stats() {
    Collection<CertificateStatusUpdateForCareType> notifs = notificationStoreV3.getNotifications();
    Map<String, List<NotificationStubEntry>> stringListMap =
        new StatTransformerUtil().toStat(notifs);
    StringBuilder buf = new StringBuilder();
    for (Map.Entry<String, List<NotificationStubEntry>> entry : stringListMap.entrySet()) {
      buf.append("---- ").append(entry.getKey()).append(" ----\n");
      entry.getValue().stream()
          .sorted(Comparator.comparing(NotificationStubEntry::getHandelseTid))
          .forEach(
              ie ->
                  buf.append(ie.getHandelseTid().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                      .append("\t")
                      .append(ie.getHandelseKod())
                      .append("\n"));
      buf.append("-----------------------------------------------\n\n");
    }
    return ResponseEntity.ok(buf.toString());
  }

  @PostMapping("/clear")
  public ResponseEntity<Void> clear() {
    notificationStoreV3.clear();
    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/notifieringar/v3/emulateError", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getErrorCode() {
    return ResponseEntity.ok(
        "Stub is set to emulateError with code " + stubStateBean.getErrorCode());
  }

  @GetMapping(
      value = "/notifieringar/v3/emulateError/{errorCode}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> setErrorCode(@PathVariable("errorCode") String errorCode) {
    stubStateBean.setErrorCode(errorCode);
    return ResponseEntity.ok("Stub set to emulateError with code " + errorCode);
  }
}
