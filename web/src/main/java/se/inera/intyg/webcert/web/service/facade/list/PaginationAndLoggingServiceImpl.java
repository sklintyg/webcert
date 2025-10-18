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

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.web.converter.FilterConverter;
import se.inera.intyg.webcert.web.service.arende.ArendeServiceImpl;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@Service
public class PaginationAndLoggingServiceImpl implements PaginationAndLoggingService {

    private final LogService logService;
    private final AuthoritiesHelper authoritiesHelper;

    @Autowired
    public PaginationAndLoggingServiceImpl(LogService logService, AuthoritiesHelper authoritiesHelper) {
        this.logService = logService;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public List<ArendeListItem> get(QueryFragaSvarParameter filterParameters, List<ArendeListItem> results, WebCertUser user) {
        Set<String> intygstyperForPrivilege = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        Filter filter;

        if (!Strings.isNullOrEmpty(filterParameters.getEnhetId())) {
            filter = FilterConverter.convert(filterParameters, Collections.singletonList(filterParameters.getEnhetId()),
                intygstyperForPrivilege);
        } else {
            filter = FilterConverter.convert(filterParameters, user.getIdsOfSelectedVardenhet(), intygstyperForPrivilege);
        }

        final var list = results.stream()
            .sorted((getComparator(filterParameters.getOrderBy(), filterParameters.getOrderAscending())))
            .collect(Collectors.toList());

        List<ArendeListItem> resultList = list
            .subList(filter.getStartFrom(), Math.min(filter.getPageSize() + filter.getStartFrom(), list.size()));

        resultList.stream().map(ArendeListItem::getPatientId).distinct().forEach(patient -> logService.logReadLevelTwo(user, patient));

        return resultList;
    }

    private Comparator<ArendeListItem> getComparator(String orderBy, Boolean ascending) {
        Comparator<ArendeListItem> comparator;
        if (orderBy == null) {
            comparator = Comparator.comparing(ArendeListItem::getReceivedDate);
        } else {
            switch (orderBy) {
                case "amne":
                    comparator = Comparator.comparing(
                        a -> ArendeServiceImpl.getAmneString(a.getAmne(), a.getStatus(), a.isPaminnelse(), a.getFragestallare())
                    );
                    break;
                case "fragestallare":
                    comparator = Comparator.comparing(ArendeListItem::getFragestallare);
                    break;
                case "patientId":
                    comparator = Comparator.comparing(ArendeListItem::getPatientId);
                    break;
                case "signeratAvNamn":
                    comparator = Comparator.comparing(ArendeListItem::getSigneratAvNamn);
                    break;
                case "vidarebefordrad":
                    comparator = Comparator.comparing(ArendeListItem::isVidarebefordrad);
                    break;
                case "receivedDate":
                default:
                    comparator = Comparator.comparing(ArendeListItem::getReceivedDate);
                    break;
            }
        }

        if (ascending == null || !ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

}
