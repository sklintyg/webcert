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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.service.facade.list.filter.CertificateFilterConverter;

import java.util.stream.Collectors;

@Service
public class ListQuestionsFacadeServiceImpl implements ListSignedCertificatesFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ListQuestionsFacadeServiceImpl.class);

    private final CertificateFilterConverter certificateFilterConverter;
    private final CertificateListItemConverter certificateListItemConverter;
    private ArendeService arendeService;

    @Autowired
    public ListQuestionsFacadeServiceImpl(CertificateFilterConverter certificateFilterConverter,
                                          CertificateListItemConverter certificateListItemConverter,
                                          ArendeService arendeService) {
        this.certificateFilterConverter = certificateFilterConverter;
        this.certificateListItemConverter = certificateListItemConverter;
        this.arendeService = arendeService;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        LOG.debug("Fetching certificates with questions");

        final var convertedFilter = certificateFilterConverter.convert(filter);

        final var listResponse = arendeService.filterArende(convertedFilter);
        final var convertedList = listResponse.getResults()
                .stream()
                .map(certificateListItemConverter::convert)
                .collect(Collectors.toList());

        return new ListInfo(listResponse.getTotalCount(), convertedList);
    }
}
