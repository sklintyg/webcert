/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterTextValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.previous.CertificateForPatientService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
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
    private Cache redisCachePrevious;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private AuthoritiesValidator authoritiesValidator;

    @Mock
    private GetStaffInfoFacadeService getStaffInfoFacadeService;

    @Mock
    private IntygService intygService;

    @Mock
    private ResourceLinkHelper resourceLinkListHelper;

    @Mock
    private IntygDraftDecorator intygDraftDecorator;

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
    private UtkastRepository utkastRepository;

    @Mock
    private LogService logService;

    @Mock
    private CertificateForPatientService certificateForPatientServiceImpl;
    @InjectMocks
    private ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService;

    private static final Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = Pair.of(TestIntygFactory.createListWithIntygItems(),
        false);

    @Test
    void shouldReturnListInfoWithPaginatedList() throws IOException, ModuleNotFoundException {
        final var listFilter = getTestListFilter();
        final var listOfUnits = List.of("unitId");
        final var expectedResult = List.of(new CertificateListItem());
        final var webCertUser = new WebCertUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(getStaffInfoFacadeService.getIdsOfSelectedUnit()).thenReturn(listOfUnits);
        when(listPaginationHelper.paginate(anyList(), any())).thenReturn(expectedResult);
        when(certificateListItemConverter.convert(any(), any())).thenReturn(new CertificateListItem());
        when(certificateForPatientServiceImpl.get(any(), anyList(), any(), anyList())).thenReturn(
            TestIntygFactory.createListWithIntygItems());

        final var actualResult = listPreviousCertificatesFacadeService.get(listFilter);

        assertEquals(expectedResult, actualResult.getList());
    }

    @Test
    void shouldReturnListInfoWithTotalListCount() throws IOException, ModuleNotFoundException {
        final var listFilter = getTestListFilter();
        final var listOfUnits = List.of("unitId");
        final var expectedResult = List.of(new CertificateListItem(), new CertificateListItem());
        final var webCertUser = new WebCertUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(getStaffInfoFacadeService.getIdsOfSelectedUnit()).thenReturn(listOfUnits);
        when(listPaginationHelper.paginate(anyList(), any())).thenReturn(expectedResult);
        when(certificateListItemConverter.convert(any(), any())).thenReturn(new CertificateListItem());
        when(certificateForPatientServiceImpl.get(any(), anyList(), any(), anyList())).thenReturn(
            TestIntygFactory.createListWithIntygItems());

        final var actualResult = listPreviousCertificatesFacadeService.get(listFilter);

        assertEquals(expectedResult.size(), actualResult.getTotalCount());
    }

    private ListFilter getTestListFilter() {
        final var listFilter = new ListFilter();
        listFilter.addValue(new ListFilterPersonIdValue("19121212-1212"), "PATIENT_ID");
        listFilter.addValue(new ListFilterTextValue(CERTIFICATE_TYPE_NAME.getName()), "ORDER_BY");
        listFilter.addValue(new ListFilterBooleanValue(false), "ASCENDING");
        return listFilter;
    }
}
