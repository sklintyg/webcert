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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import jakarta.xml.bind.JAXBElement;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesWithQARequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.List;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@ExtendWith(MockitoExtension.class)
class GetCertificatesWithQAFromCertificateServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_ID_FROM_WC = "certificateIdFromWc";
    @Mock
    ListItemNotificationDecorator listItemNotificationDecorator;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    GetCertificatesWithQAFromCertificateService getCertificatesWithQAFromCertificateService;

    @Test
    void shallReturnEmptyListIfCertificateServiceProfileIsNotActive() {
        final var notifications = Collections.singletonList(new Handelse());
        doReturn(false).when(certificateServiceProfile).active();
        assertEquals(Collections.emptyList(), getCertificatesWithQAFromCertificateService.get(notifications));
    }

    @Test
    void shallReturnEmptyListIfNotificationsIsEmpty() {
        doReturn(true).when(certificateServiceProfile).active();
        assertEquals(Collections.emptyList(), getCertificatesWithQAFromCertificateService.get(Collections.emptyList()));
    }

    @Test
    void shallReturnListOfListItemIfCertificateServiceProfileIsActive() {
        final var notification = new Handelse();
        notification.setIntygsId(CERTIFICATE_ID);
        final var notifications = Collections.singletonList(notification);
        final var certificatesWithQARequestDTO = CertificatesWithQARequestDTO.builder().build();
        final var encodedXml = "encodedXml";

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(certificatesWithQARequestDTO).when(csIntegrationRequestFactory)
            .getCertificatesWithQARequestDTO(java.util.List.of(CERTIFICATE_ID));
        doReturn(encodedXml).when(csIntegrationService).getCertificatesWithQA(certificatesWithQARequestDTO);

        final var listCertificatesForCareWithQAResponseType = new ListCertificatesForCareWithQAResponseType();
        final var list = new List();
        final var listItem = new ListItem();
        final var intyg = new Intyg();
        final var intygId = new IntygId();
        intygId.setExtension(CERTIFICATE_ID);
        intyg.setIntygsId(intygId);
        listItem.setIntyg(intyg);
        list.getItem().add(listItem);
        listCertificatesForCareWithQAResponseType.setList(list);
        final var expectedListItem = list.getItem();
        final var jaxbElement = mock(JAXBElement.class);

        try (MockedStatic<XmlMarshallerHelper> xmlMarshallerHelperMockedStatic = mockStatic(
            XmlMarshallerHelper.class)) {
            xmlMarshallerHelperMockedStatic.when(() -> XmlMarshallerHelper.unmarshal(anyString()))
                .thenReturn(jaxbElement);
            doReturn(listCertificatesForCareWithQAResponseType).when(jaxbElement).getValue();

            final var actualListItem = getCertificatesWithQAFromCertificateService.get(notifications);
            assertEquals(expectedListItem, actualListItem);
        }
    }

    @Test
    void shallCallCertificateServiceWithDistinctListOfCertificateIds() {
        final var expectedCertificateIds = java.util.List.of(CERTIFICATE_ID);
        final var notification1 = new Handelse();
        notification1.setIntygsId(CERTIFICATE_ID);
        final var notification2 = new Handelse();
        notification2.setIntygsId(CERTIFICATE_ID);
        final var notifications = java.util.List.of(notification1, notification2);
        final var encodedXml = "encodedXml";
        final var listArgumentCaptor = ArgumentCaptor.forClass(java.util.List.class);

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(encodedXml).when(csIntegrationService).getCertificatesWithQA(any());

        final var listCertificatesForCareWithQAResponseType = new ListCertificatesForCareWithQAResponseType();
        final var list = new List();
        final var listItem = new ListItem();
        final var intyg = new Intyg();
        final var intygId = new IntygId();
        intygId.setExtension(CERTIFICATE_ID);
        intyg.setIntygsId(intygId);
        listItem.setIntyg(intyg);
        list.getItem().add(listItem);
        listCertificatesForCareWithQAResponseType.setList(list);

        final var jaxbElement = mock(JAXBElement.class);

        try (MockedStatic<XmlMarshallerHelper> xmlMarshallerHelperMockedStatic = mockStatic(
            XmlMarshallerHelper.class)) {
            xmlMarshallerHelperMockedStatic.when(() -> XmlMarshallerHelper.unmarshal(anyString()))
                .thenReturn(jaxbElement);
            doReturn(listCertificatesForCareWithQAResponseType).when(jaxbElement).getValue();

            getCertificatesWithQAFromCertificateService.get(notifications);
            verify(csIntegrationRequestFactory).getCertificatesWithQARequestDTO(listArgumentCaptor.capture());
            assertEquals(expectedCertificateIds, listArgumentCaptor.getValue());
        }
    }

    @Test
    void shallFilterNotificationsToOnlyContainNotificationsForCertificatesFromCertificateService() {
        final var notification1 = new Handelse();
        final var notification2 = new Handelse();
        notification1.setIntygsId(CERTIFICATE_ID);
        notification2.setIntygsId(CERTIFICATE_ID_FROM_WC);
        final var notifications = java.util.List.of(notification1, notification2);
        final var certificatesWithQARequestDTO = CertificatesWithQARequestDTO.builder().build();
        final var encodedXml = "encodedXml";

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(certificatesWithQARequestDTO).when(csIntegrationRequestFactory)
            .getCertificatesWithQARequestDTO(java.util.List.of(CERTIFICATE_ID, CERTIFICATE_ID_FROM_WC));
        doReturn(encodedXml).when(csIntegrationService).getCertificatesWithQA(certificatesWithQARequestDTO);

        final var listCertificatesForCareWithQAResponseType = new ListCertificatesForCareWithQAResponseType();
        final var list = new List();
        final var listItem = new ListItem();
        final var intyg = new Intyg();
        final var intygId = new IntygId();
        intygId.setExtension(CERTIFICATE_ID);
        intyg.setIntygsId(intygId);
        listItem.setIntyg(intyg);
        list.getItem().add(listItem);
        listCertificatesForCareWithQAResponseType.setList(list);

        final var jaxbElement = mock(JAXBElement.class);

        try (MockedStatic<XmlMarshallerHelper> xmlMarshallerHelperMockedStatic = mockStatic(
            XmlMarshallerHelper.class)) {
            xmlMarshallerHelperMockedStatic.when(() -> XmlMarshallerHelper.unmarshal(anyString()))
                .thenReturn(jaxbElement);
            doReturn(listCertificatesForCareWithQAResponseType).when(jaxbElement).getValue();

            getCertificatesWithQAFromCertificateService.get(notifications);
            verify(listItemNotificationDecorator).decorate(list.getItem(), java.util.List.of(notification1));
        }
    }
}
