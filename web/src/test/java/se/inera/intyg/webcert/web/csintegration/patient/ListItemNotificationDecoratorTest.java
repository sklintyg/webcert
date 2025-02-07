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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa.HandelseFactory;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.HandelseList;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

class ListItemNotificationDecoratorTest {

    private static final String CERTIFICATE_ID_1 = "CERTIFICATE_1";
    private static final String CERTIFICATE_ID_2 = "CERTIFICATE_2";

    ListItemNotificationDecorator listItemNotificationDecorator;

    @BeforeEach
    void setUp() {
        listItemNotificationDecorator = new ListItemNotificationDecorator();
    }

    @Test
    void shallReturnEmptyListIfListItemsIsEmpty() {
        final var expectedResult = new ArrayList<ListItem>();
        listItemNotificationDecorator.decorate(expectedResult, Collections.emptyList());
        assertEquals(Collections.emptyList(), expectedResult);
    }

    @Test
    void shallReturnListOfListItemsWithNotification() {
        final var listItem1 = buildListItem(CERTIFICATE_ID_1);
        final var listItem2 = buildListItem(CERTIFICATE_ID_2);
        final var notifications = List.of(buildHandelse(CERTIFICATE_ID_1), buildHandelse(CERTIFICATE_ID_2));

        final var handelseList1 = new HandelseList();
        final var listItem1Notifications1 = notifications.stream()
            .filter(notification -> notification.getIntygsId().equals(listItem1.getIntyg().getIntygsId().getExtension()))
            .map(HandelseFactory::toHandelse)
            .collect(Collectors.toList());

        final var handelseList2 = new HandelseList();
        final var listItem1Notifications2 = notifications.stream()
            .filter(notification -> notification.getIntygsId().equals(listItem2.getIntyg().getIntygsId().getExtension()))
            .map(HandelseFactory::toHandelse)
            .collect(Collectors.toList());

        handelseList1.getHandelse().addAll(listItem1Notifications1);
        handelseList2.getHandelse().addAll(listItem1Notifications2);

        final var listItems = List.of(listItem1, listItem2);

        listItemNotificationDecorator.decorate(listItems, notifications);

        assertAll(
            () -> assertEquals(handelseList1.getHandelse().get(0).getHandelsekod().getCode(),
                listItem1.getHandelser().getHandelse().get(0).getHandelsekod().getCode()),
            () -> assertEquals(handelseList2.getHandelse().get(0).getHandelsekod().getCode(),
                listItem2.getHandelser().getHandelse().get(0).getHandelsekod().getCode()),
            () -> assertEquals(handelseList1.getHandelse().get(0).getSistaDatumForSvar(),
                listItem1.getHandelser().getHandelse().get(0).getSistaDatumForSvar()),
            () -> assertEquals(handelseList2.getHandelse().get(0).getTidpunkt(),
                listItem2.getHandelser().getHandelse().get(0).getTidpunkt())
        );
    }

    private Handelse buildHandelse(String certificateId) {
        final var event = new Handelse();
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId(certificateId);
        event.setSistaDatumForSvar(LocalDate.now());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private ListItem buildListItem(String certificateId) {
        final var listItem = new ListItem();
        final var intyg = new Intyg();
        final var intygId = new IntygId();
        intygId.setExtension(certificateId);
        intyg.setIntygsId(intygId);
        listItem.setIntyg(intyg);
        return listItem;
    }
}
