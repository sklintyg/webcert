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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType.CERTIFICATE_TYPE_NAME;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesAggregator;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterTextValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.previous.CertificateForPatientService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@ExtendWith(MockitoExtension.class)
class ListPreviousCertificatesFacadeServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private AuthoritiesValidator authoritiesValidator;

    @Mock
    private GetStaffInfoFacadeService getStaffInfoFacadeService;

    @Mock
    private ResourceLinkHelper resourceLinkListHelper;

    @Mock
    private ListDecorator listDecorator;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @Mock
    private ListSortHelper listSortHelper;

    @Mock
    private CertificateListItemConverter certificateListItemConverter;

    @Mock
    private ListPaginationHelper listPaginationHelper;

    @Mock
    private LogService logService;

    @Mock
    private CertificateForPatientService certificateForPatientService;

    @Mock
    private ListCertificatesAggregator listCertificatesAggregator;

    @InjectMocks
    private ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService;

    public static final ListIntygEntry CERTIFICATE_FROM_CS = new ListIntygEntry();
    private static final List<ListIntygEntry> LIST_FROM_CERTIFICATE_SERVICE = List.of(CERTIFICATE_FROM_CS);
    private static final ListFilter LIST_FILTER = getTestListFilter();
    private static final List<String> UNITS = List.of("unitId");
    private static final List<CertificateListItem> EXPECTED_LIST = List.of(new CertificateListItem(), new CertificateListItem());

    @BeforeEach
    void setup() {
        final var webCertUser = new WebCertUser();
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        when(patientDetailsResolver.getSekretessStatus(Personnummer.createPersonnummer("19121212-1212").orElseThrow()))
            .thenReturn(SekretessStatus.FALSE);

        when(getStaffInfoFacadeService.getIdsOfSelectedUnit()).thenReturn(UNITS);
        when(listPaginationHelper.paginate(anyList(), any())).thenReturn(EXPECTED_LIST);
        when(certificateListItemConverter.convert(any(), any())).thenReturn(new CertificateListItem());
        when(certificateForPatientService.get(any(), any(), anyList())).thenReturn(
            TestIntygFactory.createListWithIntygItems());

        when(listCertificatesAggregator.listCertificatesForPatient("19121212-1212"))
            .thenReturn(LIST_FROM_CERTIFICATE_SERVICE);
    }

    @Test
    void shouldReturnListInfoWithPaginatedList() throws IOException, ModuleNotFoundException {
        final var actualResult = listPreviousCertificatesFacadeService.get(LIST_FILTER);

        assertEquals(EXPECTED_LIST, actualResult.getList());
    }

    @Test
    void shouldReturnListInfoWithTotalListCountIncludingCertificatesFromCS() throws IOException, ModuleNotFoundException {
        final var actualResult = listPreviousCertificatesFacadeService.get(LIST_FILTER);

        assertEquals(EXPECTED_LIST.size() + LIST_FROM_CERTIFICATE_SERVICE.size(), actualResult.getTotalCount());
    }

    private static ListFilter getTestListFilter() {
        final var listFilter = new ListFilter();
        listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");
        listFilter.addValue(new ListFilterTextValue(CERTIFICATE_TYPE_NAME.getName()), "ORDER_BY");
        listFilter.addValue(new ListFilterBooleanValue(false), "ASCENDING");
        return listFilter;
    }
}
