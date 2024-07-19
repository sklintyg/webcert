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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest.Builder;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@ExtendWith(MockitoExtension.class)
class PatientListItemsWithQaServiceTest {

    private static final String CERTIFICATE_ID_1 = "CERTIFICATE_1";
    private static final String CERTIFICATE_ID_2 = "CERTIFICATE_2";
    @Mock
    NotificationService notificationService;
    @InjectMocks
    PatientListItemsWithQaService patientListItemsWithQaService;

    @Test
    void shallReturnEmptyListIfListItemsIsEmpty() {
        final var intygWithNotificationsRequest = new Builder().build();
        assertEquals(Collections.emptyList(),
            patientListItemsWithQaService.get(intygWithNotificationsRequest, Collections.emptyList()));
    }

    @Test
    void shallReturnListOfListItemsWithNotification() {
        final var intygWithNotificationsRequest = new Builder().build();
        final var notifications = List.of(buildHandelse(CERTIFICATE_ID_1), buildHandelse(CERTIFICATE_ID_2));
        final var expectedListItems = List.of(buildListItem(CERTIFICATE_ID_1), buildListItem(CERTIFICATE_ID_2));
        doReturn(notifications).when(notificationService).findNotifications(intygWithNotificationsRequest);

        final var actualListItems = patientListItemsWithQaService.get(intygWithNotificationsRequest, expectedListItems);
        assertEquals(expectedListItems, actualListItems);
    }

    @Test
    void shallFilterOutListItemsWithoutNotification() {
        final var intygWithNotificationsRequest = new Builder().build();
        final var notifications = List.of(buildHandelse(CERTIFICATE_ID_1));
        final var listItem = buildListItem(CERTIFICATE_ID_1);
        final var expectedListItems = List.of(listItem);
        final var listItems = List.of(listItem, buildListItem(CERTIFICATE_ID_2));

        doReturn(notifications).when(notificationService).findNotifications(intygWithNotificationsRequest);

        final var actualListItems = patientListItemsWithQaService.get(intygWithNotificationsRequest, listItems);
        assertEquals(expectedListItems, actualListItems);
    }

    @Test
    void shallReturnEmptyListIfNoNotificationsAreFound() {
        final var intygWithNotificationsRequest = new Builder().build();
        final var listItems = List.of(buildListItem(CERTIFICATE_ID_1), buildListItem(CERTIFICATE_ID_2));
        doReturn(Collections.emptyList()).when(notificationService).findNotifications(intygWithNotificationsRequest);

        final var actualListItems = patientListItemsWithQaService.get(intygWithNotificationsRequest, listItems);
        assertEquals(Collections.emptyList(), actualListItems);
    }

    private Handelse buildHandelse(String certificateId) {
        final var event = new Handelse();
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId(certificateId);
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