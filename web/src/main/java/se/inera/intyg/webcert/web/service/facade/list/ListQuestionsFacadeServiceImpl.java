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
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.service.facade.list.filter.CertificateFilterConverter;
import se.inera.intyg.webcert.web.service.facade.list.filter.QuestionFilterConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

import java.util.stream.Collectors;

@Service
public class ListQuestionsFacadeServiceImpl implements ListSignedCertificatesFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ListQuestionsFacadeServiceImpl.class);

    private final QuestionFilterConverter questionFilterConverter;
    private final CertificateListItemConverter certificateListItemConverter;
    private final ArendeService arendeService;
    private final WebCertUserService webCertUserService;
    private final CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    public ListQuestionsFacadeServiceImpl(QuestionFilterConverter questionFilterConverter,
                                          CertificateListItemConverter certificateListItemConverter,
                                          ArendeService arendeService,
                                          WebCertUserService webCertUserService,
                                          CertificateAccessServiceHelper certificateAccessServiceHelper) {
        this.questionFilterConverter = questionFilterConverter;
        this.certificateListItemConverter = certificateListItemConverter;
        this.arendeService = arendeService;
        this.webCertUserService = webCertUserService;
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        LOG.debug("Fetching certificates with questions");

        final var convertedFilter = questionFilterConverter.convert(filter);

        final var listResponse = arendeService.filterArende(convertedFilter, true);
        final var convertedList = listResponse.getResults()
                .stream()
                .map(this::decorateWithResourceLinks)
                .map(certificateListItemConverter::convert)
                .collect(Collectors.toList());

        return new ListInfo(listResponse.getTotalCount(), convertedList);
    }

    private Vardenhet getUnit() {
        final var user = webCertUserService.getUser();
        final var unit = user.getValdVardenhet();
        final var careProvider = user.getValdVardgivare();

        final var convertedCareProvider = new Vardgivare();
        convertedCareProvider.setVardgivarid(careProvider.getId());

        final var convertedUnit = new Vardenhet();
        convertedUnit.setEnhetsid(unit.getId());
        convertedUnit.setVardgivare(convertedCareProvider);

        return convertedUnit;
    }

    private ArendeListItem decorateWithResourceLinks(ArendeListItem item) {
        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
                item.getIntygTyp(),
                null,
                getUnit(),
                Personnummer.createPersonnummer(item.getPatientId()).orElseThrow(),
                item.isTestIntyg());

        if (certificateAccessServiceHelper.isAllowToForwardQuestions(accessEvaluationParameters)) {
            item.addLink(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        }

        if (certificateAccessServiceHelper.isAllowToReadQuestions(accessEvaluationParameters)) {
            item.addLink(new ActionLink(ActionLinkType.LASA_FRAGA));
        }

        return item;
    }
}
