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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import javax.xml.bind.JAXBElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PatientCertificatesWithQARequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest.Builder;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.List;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;

@ExtendWith(MockitoExtension.class)
class GetPatientCertificatesWithQAFromCertificateServiceTest {

    @Mock
    PatientCertificatesWithQaService patientCertificatesWithQaService;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    GetPatientCertificatesWithQAFromCertificateService getPatientCertificatesWithQAFromCertificateService;

    @Test
    void shallReturnEmptyListIfCertificateServiceProfileIsNotActive() {
        final var request = new Builder().build();
        doReturn(false).when(certificateServiceProfile).active();
        assertEquals(Collections.emptyList(), getPatientCertificatesWithQAFromCertificateService.get(request));
    }

    @Test
    void shallReturnListOfListItemIfCertificateServiceProfileIsActive() {
        final var request = new Builder().build();
        final var certificatesWithQARequestDTO = PatientCertificatesWithQARequestDTO.builder().build();
        final var encodedXml = "encodedXml";

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(certificatesWithQARequestDTO).when(csIntegrationRequestFactory).getPatientCertificatesWithQARequestDTO(request);
        doReturn(encodedXml).when(csIntegrationService).getPatientCertificatesWithQA(certificatesWithQARequestDTO);

        final var listCertificatesForCareWithQAResponseType = new ListCertificatesForCareWithQAResponseType();
        final var list = new List();
        list.getItem().add(new ListItem());
        listCertificatesForCareWithQAResponseType.setList(list);
        final var expectedListItem = list.getItem();
        final var jaxbElement = mock(JAXBElement.class);

        try (MockedStatic<XmlMarshallerHelper> xmlMarshallerHelperMockedStatic = mockStatic(
            XmlMarshallerHelper.class)) {
            xmlMarshallerHelperMockedStatic.when(() -> XmlMarshallerHelper.unmarshal(anyString()))
                .thenReturn(jaxbElement);
            doReturn(listCertificatesForCareWithQAResponseType).when(jaxbElement).getValue();
            doReturn(expectedListItem).when(patientCertificatesWithQaService).get(request, list.getItem());

            final var actualListItem = getPatientCertificatesWithQAFromCertificateService.get(request);
            assertEquals(expectedListItem, actualListItem);
        }
    }
}