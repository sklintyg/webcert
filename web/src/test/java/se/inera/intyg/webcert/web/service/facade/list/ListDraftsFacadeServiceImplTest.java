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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesAggregator;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.filter.DraftFilterConverter;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@ExtendWith(MockitoExtension.class)
class ListDraftsFacadeServiceImplTest {

    private static final ListFilter LIST_FILTER = new ListFilter();
    @Mock
    ListCertificatesAggregator listCertificatesAggregator;
    @Mock
    WebCertUserService webCertUserService;
    @Mock
    UtkastService utkastService;
    @Mock
    LogService logService;
    @Mock
    DraftFilterConverter draftFilterConverter;
    @Mock
    ListPaginationHelper listPaginationHelper;
    @Mock
    ListSortHelper listSortHelper;
    @Mock
    ListDecorator listDecorator;
    @Mock
    CertificateListItemConverter certificateListItemConverter;
    @Mock
    AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    @InjectMocks
    ListDraftsFacadeServiceImpl listDraftsFacadeService;

    private static final List<ListIntygEntry> CS_LIST = List.of(new ListIntygEntry());
    private static final List<ListIntygEntry> WC_LIST = List.of(new ListIntygEntry());

    @BeforeEach
    void setup() {
        final var user = new WebCertUser();
        when(webCertUserService.getUser()).thenReturn(user);

        final var features = new HashMap<String, Feature>();
        final var feature = new Feature();
        feature.setGlobal(true);
        features.put(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, feature);
        user.setFeatures(features);

        when(listDecorator.decorateAndFilterProtectedPerson(anyList())).thenReturn(WC_LIST);
        when(certificateListItemConverter.convert(any(), any())).thenReturn(new CertificateListItem());

        final var convertedFilter = new UtkastFilter("HSA_ID");
        convertedFilter.setOrderBy("ORDER_BY");
        convertedFilter.setPatientId("PATIENT_ID");
        convertedFilter.setOrderAscending(false);
        when(draftFilterConverter.convert(any(ListFilter.class)))
            .thenReturn(convertedFilter);
    }

    @Test
    void shouldMergeListWithCertificateServiceBeforeConverting() {
        when(listCertificatesAggregator.listCertificatesForUnit(LIST_FILTER))
            .thenReturn(CS_LIST);

        listDraftsFacadeService.get(LIST_FILTER);

        verify(certificateListItemConverter, times(2)).convert(any(ListIntygEntry.class), eq(ListType.DRAFTS));
    }

    @Test
    void shouldSetTotalCount() {
        when(listCertificatesAggregator.listCertificatesForUnit(LIST_FILTER))
            .thenReturn(CS_LIST);

        final var response = listDraftsFacadeService.get(LIST_FILTER);

        assertEquals(2, response.getTotalCount());
    }

}
