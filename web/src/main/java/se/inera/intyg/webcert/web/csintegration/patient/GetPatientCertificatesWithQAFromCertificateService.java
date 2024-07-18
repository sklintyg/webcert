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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListCertificatesForCareWithQAResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCareWithQA.v3.ListItem;

@Component
@RequiredArgsConstructor
public class GetPatientCertificatesWithQAFromCertificateService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    public List<ListItem> get(IntygWithNotificationsRequest request) {
        if (!certificateServiceProfile.active()) {
            return Collections.emptyList();
        }

        final var encodedXml = csIntegrationService.getPatientCertificatesWithQA(
            csIntegrationRequestFactory.getPatientCertificatesWithQARequestDTO(request)
        );

        final var decodedXml = new String(Base64.getDecoder().decode(encodedXml.getBytes()), StandardCharsets.UTF_8);
        final var certificatesForCareWithQAResponseType = (ListCertificatesForCareWithQAResponseType) XmlMarshallerHelper.unmarshal(
            decodedXml).getValue();

        return certificatesForCareWithQAResponseType.getList().getItem();
    }
}
