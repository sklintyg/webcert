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

package se.inera.intyg.webcert.web.csintegration.patient;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa.HandelseFactory;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;

@Service
@RequiredArgsConstructor
public class ListItemNotificationDecorator {

    public void decorate(List<ListItem> listItems, List<Handelse> notifications) {
        if (listItems.isEmpty()) {
            return;
        }

        final var notificationMap = notifications.stream()
            .collect(Collectors.groupingBy(Handelse::getIntygsId));

        listItems.forEach(
            listItem -> decorateWithNotification(listItem, notificationMap.get(listItem.getIntyg().getIntygsId().getExtension()))
        );
    }

    private void decorateWithNotification(ListItem listItem, List<Handelse> notifications) {
        final var eventList = new HandelseList();
        eventList.getHandelse().addAll(
            notifications.stream()
                .map(HandelseFactory::toHandelse)
                .collect(Collectors.toList())
        );
        listItem.setHandelser(eventList);
    }
}
