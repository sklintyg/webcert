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
package se.inera.intyg.webcert.web.service.facade.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesAggregator;
import se.inera.intyg.webcert.web.service.certificate.CertificateService;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.filter.CertificateFilterConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@ExtendWith(MockitoExtension.class)
class ListSignedCertificatesFacadeServiceImplTest {

    private static final String HSA_ID = "HSA_ID";
    private static final List<String> UNIT_IDS = List.of("ID1", "ID2");
    private static final String[] UNIT_IDS_ARRAY = {"ID1", "ID2"};
    private static final QueryIntygParameter PARAMETER = new QueryIntygParameter();
    private static final CertificateListResponse WC_RESPONSE = new CertificateListResponse();
    private static final CertificateListEntry INTYG = new CertificateListEntry();
    private static final ListIntygEntry CERTIFICATE = new ListIntygEntry();
    private static final CertificateListItem CONVERTED_INTYG = new CertificateListItem();
    private static final CertificateListItem CONVERTED_CERTIFICATE = new CertificateListItem();

    @Mock
    WebCertUserService webCertUserService;
    @Mock
    CertificateService certificateService;
    @Mock
    CertificateFilterConverter certificateFilterConverter;
    @Mock
    CertificateListItemConverter certificateListItemConverter;
    @Mock
    GetStaffInfoFacadeService getStaffInfoFacadeService;
    @Mock
    ListCertificatesAggregator listCertificatesAggregator;
    @Mock
    ListSortHelper listSortHelper;

    @InjectMocks
    ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService;

    @BeforeEach
    void setup() {
        final var user = mock(WebCertUser.class);
        final var unit = mock(SelectableVardenhet.class);
        when(webCertUserService.getUser())
            .thenReturn(user);
        when(user.getHsaId())
            .thenReturn(HSA_ID);
        when(user.getValdVardenhet())
            .thenReturn(unit);
        when(unit.getId())
            .thenReturn("UNIT_ID");

        when(getStaffInfoFacadeService.getIdsOfSelectedUnit())
            .thenReturn(UNIT_IDS);
        when(certificateFilterConverter.convert(any(), any(), any()))
            .thenReturn(PARAMETER);

        WC_RESPONSE.setCertificates(List.of(INTYG));
        when(certificateService.listCertificatesForDoctor(PARAMETER))
            .thenReturn(WC_RESPONSE);
        when(certificateListItemConverter.convert(INTYG))
            .thenReturn(CONVERTED_INTYG);

        when(listCertificatesAggregator.listCertificatesForDoctor(PARAMETER))
            .thenReturn(List.of(CERTIFICATE));
        when(certificateListItemConverter.convert(CERTIFICATE, ListType.CERTIFICATES))
            .thenReturn(CONVERTED_CERTIFICATE);

        when(listSortHelper.sort(List.of(CONVERTED_INTYG, CONVERTED_CERTIFICATE), "", true))
            .thenReturn(List.of(CONVERTED_CERTIFICATE, CONVERTED_INTYG));
        PARAMETER.setOrderAscending(true);
    }

    @Nested
    class NotPaginatedList {

        @BeforeEach
        void setup() {
            PARAMETER.setPageSize(10);
            PARAMETER.setStartFrom(0);
        }

        @Test
        void shouldConvertFilter() {
            final var filter = new ListFilter();
            listSignedCertificatesFacadeService.get(filter);

            verify(certificateFilterConverter).convert(filter, HSA_ID, UNIT_IDS_ARRAY);
        }

        @Test
        void shouldCallITWithPageSizeNegativeAndStartFrom0() {
            final var argumentCaptor = ArgumentCaptor.forClass(QueryIntygParameter.class);
            final var filter = new ListFilter();
            listSignedCertificatesFacadeService.get(filter);

            verify(certificateService).listCertificatesForDoctor(argumentCaptor.capture());
            assertEquals(-1, argumentCaptor.getValue().getPageSize());
            assertEquals(0, argumentCaptor.getValue().getStartFrom());
        }

        @Test
        void shouldSortMergedList() {
            final var argumentCaptor = ArgumentCaptor.forClass(List.class);
            final var filter = new ListFilter();
            listSignedCertificatesFacadeService.get(filter);

            verify(listSortHelper).sort(argumentCaptor.capture(), anyString(), anyBoolean());
            assertEquals(2, argumentCaptor.getValue().size());
            assertTrue(argumentCaptor.getValue().contains(CONVERTED_CERTIFICATE));
            assertTrue(argumentCaptor.getValue().contains(CONVERTED_INTYG));
        }

        @Test
        void shouldReturnTotalCountOfTotalMergedList() {
            final var filter = new ListFilter();
            final var response = listSignedCertificatesFacadeService.get(filter);

            assertEquals(2, response.getTotalCount());
        }

        @Test
        void shouldReturnFullListSinceInsidePagination() {
            final var filter = new ListFilter();
            final var response = listSignedCertificatesFacadeService.get(filter);

            assertEquals(2, response.getList().size());
            assertTrue(response.getList().contains(CONVERTED_CERTIFICATE));
            assertTrue(response.getList().contains(CONVERTED_INTYG));
        }
    }

    @Nested
    class PaginatedList {

        @BeforeEach
        void setup() {
            PARAMETER.setPageSize(1);
            PARAMETER.setStartFrom(0);
        }

        @Test
        void shouldReturnTotalCountOfTotalMergedList() {
            final var filter = new ListFilter();
            final var response = listSignedCertificatesFacadeService.get(filter);

            assertEquals(2, response.getTotalCount());
        }

        @Test
        void shouldReturnPaginatedList() {
            final var filter = new ListFilter();
            final var response = listSignedCertificatesFacadeService.get(filter);

            assertEquals(1, response.getList().size());
            assertTrue(response.getList().contains(CONVERTED_CERTIFICATE));
        }


        @Test
        void shouldReturnPaginatedListOnlyIncludingLastElement() {
            PARAMETER.setStartFrom(1);
            final var filter = new ListFilter();
            final var response = listSignedCertificatesFacadeService.get(filter);

            assertEquals(1, response.getList().size());
            assertTrue(response.getList().contains(CONVERTED_INTYG));
        }
    }
}
