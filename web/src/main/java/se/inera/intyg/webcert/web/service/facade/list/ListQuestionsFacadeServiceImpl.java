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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.certificate.ListCertificateQuestionsFromCS;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.service.facade.list.filter.QuestionFilterConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Service
public class ListQuestionsFacadeServiceImpl implements ListSignedCertificatesFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ListQuestionsFacadeServiceImpl.class);

    private final QuestionFilterConverter questionFilterConverter;
    private final CertificateListItemConverter certificateListItemConverter;
    private final ArendeService arendeService;
    private final WebCertUserService webCertUserService;
    private final CertificateAccessServiceHelper certificateAccessServiceHelper;
    private final PaginationAndLoggingService paginationAndLoggingService;
    private final ListCertificateQuestionsFromCS listCertificateQuestionsFromCS;

    @Autowired
    public ListQuestionsFacadeServiceImpl(QuestionFilterConverter questionFilterConverter,
        CertificateListItemConverter certificateListItemConverter,
        ArendeService arendeService,
        WebCertUserService webCertUserService,
        CertificateAccessServiceHelper certificateAccessServiceHelper,
        PaginationAndLoggingService paginationAndLoggingService,
        ListCertificateQuestionsFromCS listCertificateQuestionsFromCS) {
        this.questionFilterConverter = questionFilterConverter;
        this.certificateListItemConverter = certificateListItemConverter;
        this.arendeService = arendeService;
        this.webCertUserService = webCertUserService;
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
        this.paginationAndLoggingService = paginationAndLoggingService;
        this.listCertificateQuestionsFromCS = listCertificateQuestionsFromCS;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        LOG.debug("Fetching certificates with questions");

        final var convertedFilter = questionFilterConverter.convert(filter);

        final var listResponseFromWC = arendeService.filterArende(convertedFilter, true);
        final var listResponseFromCS = listCertificateQuestionsFromCS.list(convertedFilter);

        final var aggregatedList = Stream.concat(
                listResponseFromWC.getResults().stream(),
                listResponseFromCS.getResults().stream())
            .toList();

        final var user = webCertUserService.getUser();
        final var paginatedList = paginationAndLoggingService.get(convertedFilter, aggregatedList, user);

        final var unit = getUnit(user);
        final var wcItemIds = listResponseFromWC.getResults().stream()
            .map(ArendeListItem::getMeddelandeId)
            .collect(Collectors.toSet());

        final var decoratedList = paginatedList.stream()
            .map(item -> {
                // Only decorate items from WC, not from CS
                if (wcItemIds.contains(item.getMeddelandeId())) {
                    return decorateWithResourceLinks(item, unit);
                }
                return item;
            })
            .toList();

        final var convertedList = decoratedList
            .stream()
            .map(certificateListItemConverter::convert)
            .toList();

        return new ListInfo(listResponseFromWC.getTotalCount() + listResponseFromCS.getTotalCount(), convertedList);
    }

    private Vardenhet getUnit(WebCertUser user) {
        final var unit = user.getValdVardenhet();
        final var careProvider = user.getValdVardgivare();

        final var convertedCareProvider = new Vardgivare();
        convertedCareProvider.setVardgivarid(careProvider.getId());

        final var convertedUnit = new Vardenhet();
        convertedUnit.setEnhetsid(unit.getId());
        convertedUnit.setVardgivare(convertedCareProvider);

        return convertedUnit;
    }

    private ArendeListItem decorateWithResourceLinks(ArendeListItem item, Vardenhet unit) {
        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
            item.getIntygTyp(),
            null,
            unit,
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