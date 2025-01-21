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
package se.inera.intyg.webcert.web.service.facade.list.filter;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
public class DraftFilterConverterImpl implements DraftFilterConverter {

    private final WebCertUserService webCertUserService;

    public DraftFilterConverterImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public UtkastFilter convert(ListFilter filter) {
        return convertFilter(filter);
    }

    private UtkastFilter convertFilter(ListFilter filter) {
        final var convertedFilter = initializeFilter();

        convertedFilter.setSavedFrom(ListFilterHelper.getSavedFrom(filter));
        convertedFilter.setSavedTo(ListFilterHelper.getSavedTo(filter));
        convertedFilter.setSavedByHsaId(ListFilterHelper.getSavedBy(filter));
        convertedFilter.setPatientId(ListFilterHelper.getPatientId(filter));
        convertedFilter.setNotified(ListFilterHelper.getForwarded(filter));
        convertedFilter.setOrderBy(ListFilterHelper.getOrderBy(filter));
        convertedFilter.setOrderAscending(ListFilterHelper.getAscending(filter));
        convertedFilter.setStatusList(ListFilterHelper.getDraftStatus(filter));
        return convertedFilter;
    }

    private UtkastFilter initializeFilter() {
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();
        return new UtkastFilter(selectedUnitHsaId);
    }
}
