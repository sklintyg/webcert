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

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListPreviousCertificatesFacadeServiceImpl implements ListPreviousCertificatesFacadeService {

    private static final ListType LIST_TYPE = ListType.PREVIOUS_CERTIFICATES;
    private static final Logger LOG = LoggerFactory.getLogger(ListPreviousCertificatesFacadeServiceImpl.class);

    private static final List<UtkastStatus> ALL_DRAFTS =
                    Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_LOCKED);

    private static final List<String> CURRENT_CERTIFICATES =
                    Arrays.asList(CertificateStatus.INCOMPLETE.getName(), CertificateStatus.COMPLETE.getName(),
                    CertificateStatus.SIGNED.getName(), CertificateStatus.SENT.getName());

    private static final List<String> MODIFIED_CERTIFICATES =
                    Arrays.asList(CertificateStatus.LOCKED.getName(), CertificateStatus.RENEWED.getName(),
                    CertificateStatus.REVOKED.getName(), CertificateStatus.COMPLEMENTED.getName());

    private final WebCertUserService webCertUserService;
    private final LogService logService;
    private final ListPaginationHelper listPaginationHelper;
    private final CertificateListItemConverter certificateListItemConverter;
    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    private final PatientDetailsResolver patientDetailsResolver;
    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final IntygService intygService;
    private final AuthoritiesHelper authoritiesHelper;
    private final UtkastRepository utkastRepository;
    private final ResourceLinkHelper resourceLinkHelper;
    private final ListSortHelper listSortHelper;
    private final ListDecorator listDecorator;

    @Autowired
    public ListPreviousCertificatesFacadeServiceImpl(WebCertUserService webCertUserService, LogService logService,
                                                     ListPaginationHelper listPaginationHelper,
                                                     CertificateListItemConverter certificateListItemConverter,
                                                     PatientDetailsResolver patientDetailsResolver,
                                                     GetStaffInfoFacadeService getStaffInfoFacadeService,
                                                     IntygService intygService, AuthoritiesHelper authoritiesHelper,
                                                     UtkastRepository utkastRepository, ResourceLinkHelper resourceLinkHelper,
                                                     ListSortHelper listSortHelper, ListDecorator listDecorator) {
        this.webCertUserService = webCertUserService;
        this.logService = logService;
        this.listPaginationHelper = listPaginationHelper;
        this.certificateListItemConverter = certificateListItemConverter;
        this.patientDetailsResolver = patientDetailsResolver;
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
        this.intygService = intygService;
        this.authoritiesHelper = authoritiesHelper;
        this.utkastRepository = utkastRepository;
        this.resourceLinkHelper = resourceLinkHelper;
        this.listSortHelper = listSortHelper;
        this.listDecorator = listDecorator;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        final var patientId = formatPatientId(filter);

        final var user = webCertUserService.getUser();
        final var protectedPatientStatus = checkUserAccess(user, patientId);

        final var units = getUnits();
        final var certificates = getCertificates(patientId, units);
        final var drafts = getDrafts(user, patientId, units);

        final var mergedList = IntygDraftsConverter.merge(certificates.getLeft(), drafts);
        final var filteredList = filterProtectedPatients(protectedPatientStatus, mergedList);

        resourceLinkHelper.decorateIntygWithValidActionLinks(filteredList, patientId);
        listDecorator.decorateWithCertificateTypeName(filteredList);

        final var convertedList = convertList(filteredList);
        listSortHelper.sort(convertedList, getOrderBy(filter), getAscending(filter));

        final var filteredListOnStatus = filterListOnStatus(filter, convertedList);
        final var totalListCount = filteredListOnStatus.size();
        final var paginatedList = listPaginationHelper.paginate(filteredListOnStatus, filter);
        logListUsage(patientId, user);

        return new ListInfo(totalListCount, paginatedList);
    }

    private String getOrderBy(ListFilter filter) {
        final var value = (ListFilterTextValue) filter.getValue("ORDER_BY");
        return value.getValue();
    }

    private boolean getAscending(ListFilter filter) {
        final var value = (ListFilterBooleanValue) filter.getValue("ASCENDING");
        return value.getValue();
    }

    private Pair<List<ListIntygEntry>, Boolean> getCertificates(Personnummer patientId, List<String> units) {
        final var certificates = intygService.listIntyg(units, patientId);
        LOG.debug("Got #{} intyg", certificates.getLeft().size());
        return certificates;
    }

    private List<ListIntygEntry> filterProtectedPatients(SekretessStatus protectedPatientStatus, List<ListIntygEntry> list) {
        if (protectedPatientStatus == SekretessStatus.TRUE) {
            Set<String> allowedTypes = authoritiesHelper.getIntygstyperAllowedForSekretessmarkering();
            return list.stream().filter(certificate -> allowedTypes.contains(certificate.getIntygType())).collect(Collectors.toList());
        } else {
            return list;
        }
    }

    private Personnummer formatPatientId(ListFilter filter) {
        final var patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return Personnummer.createPersonnummer(patientId.getValue())
                .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                        String.format("Cannot create Personnummer object with invalid personId %s", patientId)));
    }

    private List<String> getUnits() {
        final var units = getStaffInfoFacadeService.getUnits();
        if (units.isEmpty()) {
            LOG.error("Current user has no assignments");
            //throw exception
        }
        return units;
    }

    private List<CertificateListItem> filterListOnStatus(ListFilter filter, List<CertificateListItem> list) {
        final var statusFilter = (ListFilterRadioValue) filter.getValue("STATUS");
        if (statusFilter == null || doesFilterMatchList(statusFilter, FilterStatusType.ALL_CERTIFICATES)) {
            return list;
        } else if (doesFilterMatchList(statusFilter, FilterStatusType.MODIFIED_CERTIFICATES)) {
            return performStatusFiltering(list, MODIFIED_CERTIFICATES);
        } else if (doesFilterMatchList(statusFilter, FilterStatusType.CURRENT_CERTIFICATES)) {
            return performStatusFiltering(list, CURRENT_CERTIFICATES);
        }
        return list;
    }

    private List<CertificateListItem> performStatusFiltering(List<CertificateListItem> list, List<String> wantedStatuses) {
        return list
                .stream()
                .filter((item) -> wantedStatuses.contains((String) item.getValue("STATUS")))
                .collect(Collectors.toList());
    }

    private boolean doesFilterMatchList(ListFilterRadioValue statusFilter, FilterStatusType statusType) {
        return statusFilter.getValue().equals(statusType.toString());
    }

    private List<Utkast> getDrafts(WebCertUser user, Personnummer patientId, List<String> units) {
        if (authoritiesValidator.given(user).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).isVerified()) {
            Set<String> intygstyper = authoritiesHelper
                    .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

            final var drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(
                    DaoUtil.formatPnrForPersistence(patientId),
                    units,
                    ALL_DRAFTS,
                    intygstyper);

            LOG.debug("Got #{} utkast", drafts.size());
            return drafts;
        } else {
            return Collections.emptyList();
        }
    }

    private SekretessStatus checkUserAccess(WebCertUser user, Personnummer patientId) {
        final var protectedPatientStatus = patientDetailsResolver.getSekretessStatus(patientId);
        if (protectedPatientStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Error checking sekretessmarkering state in PU-service.");
        }

        authoritiesValidator.given(user)
                .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                        protectedPatientStatus == SekretessStatus.TRUE)
                .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                        "User missing required privilege or cannot handle sekretessmarkerad patient"));

        return protectedPatientStatus;
    }

    private void logListUsage(Personnummer patientId, WebCertUser user) {
        logService.logListIntyg(user, patientId.getPersonnummerWithDash());
    }

    private List<CertificateListItem> convertList(List<ListIntygEntry> intygEntryList) {
        return intygEntryList.stream().map((item) -> certificateListItemConverter.convert(item, LIST_TYPE)).collect(Collectors.toList());
    }
}
