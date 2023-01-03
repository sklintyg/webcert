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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.certificate.CertificateService;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.service.facade.list.filter.CertificateFilterConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.stream.Collectors;

@Service
public class ListSignedCertificatesFacadeServiceImpl implements ListSignedCertificatesFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ListSignedCertificatesFacadeServiceImpl.class);

    private final WebCertUserService webCertUserService;
    private final CertificateService certificateService;
    private final CertificateFilterConverter certificateFilterConverter;
    private final CertificateListItemConverter certificateListItemConverter;
    private final GetStaffInfoFacadeService getStaffInfoFacadeService;

    @Autowired
    public ListSignedCertificatesFacadeServiceImpl(WebCertUserService webCertUserService,
                                                   CertificateService certificateService,
                                                   CertificateFilterConverter certificateFilterConverter,
                                                   CertificateListItemConverter certificateListItemConverter,
                                                   GetStaffInfoFacadeService getStaffInfoFacadeService) {
        this.webCertUserService = webCertUserService;
        this.certificateService = certificateService;
        this.certificateFilterConverter = certificateFilterConverter;
        this.certificateListItemConverter = certificateListItemConverter;
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        final var user = webCertUserService.getUser();
        LOG.debug("Fetching all signed intyg for doctor '{}' on unit '{}' from IT", user.getHsaId(), user.getValdVardenhet().getId());

        final var units = getUnitsForCurrentUser();
        final var convertedFilter = certificateFilterConverter.convert(filter, user.getHsaId(), units);

        final var listResponse = certificateService.listCertificatesForDoctor(convertedFilter);
        final var convertedList = listResponse.getCertificates()
                .stream()
                .map(certificateListItemConverter::convert)
                .collect(Collectors.toList());

        return new ListInfo(listResponse.getTotalCount(), convertedList);
    }

    private String[] getUnitsForCurrentUser() {
        final var units = getStaffInfoFacadeService.getIdsOfSelectedUnit();
        return units.toArray(new String[0]);
    }
}
