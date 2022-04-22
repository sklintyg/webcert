/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListDraftsFacadeServiceImpl implements ListDraftsFacadeService {

    private static final ListType LIST_TYPE = ListType.DRAFTS;

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;
    private final LogService logService;
    private final DraftFilterConverter draftFilterConverter;
    private final ListPaginationHelper listPaginationHelper;
    private final ListSortHelper listSortHelper;
    private final ListDecorator listDecorator;
    private final CertificateListItemConverter certificateListItemConverter;

    @Autowired
    public ListDraftsFacadeServiceImpl(WebCertUserService webCertUserService, UtkastService utkastService,
                                       LogService logService, DraftFilterConverter draftFilterConverter,
                                       ListPaginationHelper listPaginationHelper, ListSortHelper listSortHelper,
                                       ListDecorator listDecorator, CertificateListItemConverter certificateListItemConverter) {
        this.webCertUserService = webCertUserService;
        this.utkastService = utkastService;
        this.logService = logService;
        this.draftFilterConverter = draftFilterConverter;
        this.listPaginationHelper = listPaginationHelper;
        this.listSortHelper = listSortHelper;
        this.listDecorator = listDecorator;
        this.certificateListItemConverter = certificateListItemConverter;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        final var user = webCertUserService.getUser();
        final var convertedFilter = draftFilterConverter.convert(filter);

        final var intygEntryList = getIntygEntryList(convertedFilter);
        final var decoratedAndFilteredList = decorateList(intygEntryList);
        final var totalListCount = decoratedAndFilteredList.size();

        final var convertedList = convertList(decoratedAndFilteredList);
        final var sortedList = listSortHelper.sort(convertedList, convertedFilter.getOrderBy(), convertedFilter.getOrderAscending());
        final var paginatedList = listPaginationHelper.paginate(sortedList, filter);

        logListUsage(user, paginatedList);
        return new ListInfo(totalListCount, paginatedList);
    }

    private List<ListIntygEntry> decorateList(List<ListIntygEntry> list) {
        listDecorator.decorateWithCertificateTypeName(list);
        listDecorator.decorateWithStaffName(list);
        return listDecorator.decorateAndFilterProtectedPerson(list);
    }

    private void logListUsage(WebCertUser user, List<CertificateListItem> paginatedList) {
        paginatedList.stream().map(CertificateListItem::valueAsPatientId).distinct().forEach(
                id -> performPDLLogging(user, id)
        );
    }

    private List<CertificateListItem> convertList(List<ListIntygEntry> intygEntryList) {
        return intygEntryList.stream().map((item) -> certificateListItemConverter.convert(item, LIST_TYPE)).collect(Collectors.toList());
    }

    private List<ListIntygEntry> getIntygEntryList(UtkastFilter filter) {
        final var list = utkastService.filterIntyg(filter);
        return IntygDraftsConverter.convertUtkastsToListIntygEntries(list);
    }

    private void performPDLLogging(WebCertUser user, String patientId) {
        logService.logListIntyg(user, patientId);
    }
}
