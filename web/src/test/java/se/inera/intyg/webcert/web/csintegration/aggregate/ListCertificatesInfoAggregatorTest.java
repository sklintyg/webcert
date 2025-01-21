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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesInfoRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;

@ExtendWith(MockitoExtension.class)
class ListCertificatesInfoAggregatorTest {

    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private ListCertificatesInfoAggregator listCertificatesInfoAggregator;

    private static final GetUnitCertificatesInfoRequestDTO UNIT_REQUEST = GetUnitCertificatesInfoRequestDTO.builder().build();
    private static final List<StaffListInfo> STAFF_LIST_INFOS = List.of(StaffListInfo.builder().build());

    @Test
    void shouldReturnEmptyListIfProfileIsNotActive() {
        final var response = listCertificatesInfoAggregator.listCertificatesInfoForUnit();
        assertEquals(Collections.emptyList(), response);
        verifyNoInteractions(csIntegrationService);
    }

    @Test
    void shouldReturnListFromAPIIfProfileIsActive() {
        when(csIntegrationRequestFactory.getUnitCertificatesInfoRequest())
            .thenReturn(UNIT_REQUEST);
        when(certificateServiceProfile.active())
            .thenReturn(true);
        when(csIntegrationService.listCertificatesInfoForUnit(UNIT_REQUEST))
            .thenReturn(STAFF_LIST_INFOS);

        final var response = listCertificatesInfoAggregator.listCertificatesInfoForUnit();

        assertEquals(STAFF_LIST_INFOS, response);
    }
}
