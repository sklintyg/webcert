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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesAggregator;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeService;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterBooleanValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterRadioValue;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterTextValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.FilterStatusType;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.service.facade.list.previous.CertificateForPatientService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@Service
public class ListPreviousCertificatesFacadeServiceImpl implements ListPreviousCertificatesFacadeService {

    private static final ListType LIST_TYPE = ListType.PREVIOUS_CERTIFICATES;
    private static final List<String> CURRENT_CERTIFICATES =
        Arrays.asList(CertificateListItemStatus.INCOMPLETE.getName(), CertificateListItemStatus.COMPLETE.getName(),
            CertificateListItemStatus.SIGNED.getName(), CertificateListItemStatus.SENT.getName());

    private static final List<String> MODIFIED_CERTIFICATES =
        Arrays.asList(CertificateListItemStatus.LOCKED.getName(), CertificateListItemStatus.REVOKED.getName(),
            CertificateListItemStatus.REPLACED.getName(), CertificateListItemStatus.COMPLEMENTED.getName());

    private final WebCertUserService webCertUserService;
    private final LogService logService;
    private final ListPaginationHelper listPaginationHelper;
    private final CertificateListItemConverter certificateListItemConverter;
    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    private final PatientDetailsResolver patientDetailsResolver;
    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final AuthoritiesHelper authoritiesHelper;
    private final ResourceLinkHelper resourceLinkHelper;
    private final ListSortHelper listSortHelper;
    private final ListDecorator listDecorator;
    private final CertificateForPatientService certificateForPatientService;
    private final ListCertificatesAggregator listCertificatesAggregator;

    @Autowired
    public ListPreviousCertificatesFacadeServiceImpl(WebCertUserService webCertUserService, LogService logService,
        ListPaginationHelper listPaginationHelper,
        CertificateListItemConverter certificateListItemConverter,
        PatientDetailsResolver patientDetailsResolver,
        GetStaffInfoFacadeService getStaffInfoFacadeService,
        AuthoritiesHelper authoritiesHelper,
        ResourceLinkHelper resourceLinkHelper,
        ListSortHelper listSortHelper, ListDecorator listDecorator,
        CertificateForPatientService certificatesForPatientService, ListCertificatesAggregator listCertificatesAggregator) {
        this.webCertUserService = webCertUserService;
        this.logService = logService;
        this.listPaginationHelper = listPaginationHelper;
        this.certificateListItemConverter = certificateListItemConverter;
        this.patientDetailsResolver = patientDetailsResolver;
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
        this.authoritiesHelper = authoritiesHelper;
        this.resourceLinkHelper = resourceLinkHelper;
        this.listSortHelper = listSortHelper;
        this.listDecorator = listDecorator;
        this.certificateForPatientService = certificatesForPatientService;
        this.listCertificatesAggregator = listCertificatesAggregator;
    }

    @Override
    public ListInfo get(ListFilter filter) {
        final var patientId = formatPatientId(filter);
        final var user = webCertUserService.getUser();
        final var units = getUnits();

        final var certificatesForPatient = certificateForPatientService.get(filter, patientId, units);
        final var filteredList = filterProtectedPatients(user, patientId, certificatesForPatient);

        resourceLinkHelper.decorateIntygWithValidActionLinks(filteredList, patientId);
        listDecorator.decorateWithCertificateTypeName(filteredList);

        final var listFromCertificateService = listCertificatesAggregator.listCertificatesForPatient(patientId.getOriginalPnr());
        final var mergedList = Stream
            .concat(filteredList.stream(), listFromCertificateService.stream())
            .collect(Collectors.toList());

        final var convertedList = convertList(mergedList);
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

    private List<ListIntygEntry> filterProtectedPatients(WebCertUser user, Personnummer patientId, List<ListIntygEntry> list) {
        final var protectedPatientStatus = patientDetailsResolver.getSekretessStatus(patientId);
        if (protectedPatientStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "Error checking sekretessmarkering state in PU-service.");
        }

        if (protectedPatientStatus == SekretessStatus.FALSE) {
            return list;
        }

        return list.stream()
            .filter(certificate ->
                authoritiesValidator
                    .given(user, certificate.getIntygType())
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified()
            )
            .collect(Collectors.toList());
    }

    private Personnummer formatPatientId(ListFilter filter) {
        final var patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return Personnummer.createPersonnummer(patientId.getValue())
            .orElseThrow(() ->
                new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    String.format("Cannot create Personnummer object with invalid personId %s", patientId))
            );
    }

    private List<String> getUnits() {
        final var units = getStaffInfoFacadeService.getIdsOfSelectedUnit();
        if (units.isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Current user has no assignments");
        }
        return units;
    }

    private List<CertificateListItem> filterListOnStatus(ListFilter filter, List<CertificateListItem> list) {
        final var statusFilter = (ListFilterRadioValue) filter.getValue("STATUS");

        if (doesFilterMatchList(statusFilter, FilterStatusType.MODIFIED_CERTIFICATES)) {
            return performStatusFiltering(list, MODIFIED_CERTIFICATES);
        }

        if (doesFilterMatchList(statusFilter, FilterStatusType.CURRENT_CERTIFICATES)) {
            return performStatusFiltering(list, CURRENT_CERTIFICATES);
        }

        return list;
    }

    private List<CertificateListItem> performStatusFiltering(List<CertificateListItem> list, List<String> wantedStatuses) {
        return list.stream()
            .filter(item -> wantedStatuses.contains((String) item.getValue("STATUS")))
            .collect(Collectors.toList());
    }

    private boolean doesFilterMatchList(ListFilterRadioValue statusFilter, FilterStatusType statusType) {
        if (statusFilter == null) {
            return false;
        }

        return statusFilter.getValue().equals(statusType.toString());
    }

    private void logListUsage(Personnummer patientId, WebCertUser user) {
        logService.logReadLevelTwo(user, patientId.getPersonnummerWithDash());
    }

    private List<CertificateListItem> convertList(List<ListIntygEntry> intygEntryList) {
        return intygEntryList.stream()
            .map(item -> certificateListItemConverter.convert(item, LIST_TYPE))
            .collect(Collectors.toList());
    }
}
