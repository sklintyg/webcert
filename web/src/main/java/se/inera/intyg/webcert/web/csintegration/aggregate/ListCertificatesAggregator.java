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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@Service
@RequiredArgsConstructor
public class ListCertificatesAggregator {

    private final CSIntegrationService csIntegrationService;
    private final CSIntegrationRequestFactory csIntegrationRequestFactory;

    public List<ListIntygEntry> listCertificatesForPatient(String patientId) {
        return csIntegrationService.listCertificatesForPatient(csIntegrationRequestFactory.getPatientCertificatesRequest(patientId));
    }

    public List<ListIntygEntry> listCertificatesForUnit(ListFilter filter) {
        return csIntegrationService.listCertificatesForUnit(csIntegrationRequestFactory.getUnitCertificatesRequest(filter));
    }

    public List<ListIntygEntry> listCertificatesForDoctor(QueryIntygParameter filter) {
        return csIntegrationService.listCertificatesForUnit(csIntegrationRequestFactory.getUnitCertificatesRequest(filter));
    }
}
