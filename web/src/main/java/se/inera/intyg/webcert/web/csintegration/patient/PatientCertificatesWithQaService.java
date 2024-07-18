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

package se.inera.intyg.webcert.web.csintegration.patient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa.HandelseFactory;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;

@Service
@RequiredArgsConstructor
public class PatientCertificatesWithQaService {

    private final NotificationService notificationService;

    public List<ListItem> get(IntygWithNotificationsRequest request, List<ListItem> listItems) {
        if (listItems.isEmpty()) {
            return Collections.emptyList();
        }

        final var allNotifications = notificationService.findNotifications(request);
        final var notificationMap = allNotifications.stream()
            .collect(Collectors.groupingBy(Handelse::getIntygsId));

        final var certificatesWithQA = listItems.stream()
            .filter(listItem -> notificationMap.containsKey(listItem.getIntyg().getIntygsId().getExtension()))
            .collect(Collectors.toList());

        certificatesWithQA.forEach(
            listItem -> decorateWithNotification(listItem, notificationMap.get(listItem.getIntyg().getIntygsId().getExtension()))
        );

        return certificatesWithQA;
    }

    private void decorateWithNotification(ListItem listItem, List<Handelse> handelse) {
        final var eventList = new HandelseList();
        eventList.getHandelse().addAll(
            handelse.stream()
                .map(HandelseFactory::toHandelse)
                .collect(Collectors.toList())
        );
        listItem.setHandelser(eventList);
    }
}
