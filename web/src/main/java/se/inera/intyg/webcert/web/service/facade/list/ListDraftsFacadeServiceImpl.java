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
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.facade.list.config.*;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListDraftsFacadeServiceImpl implements ListDraftsFacadeService {
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    private final LogService logService;
    private final IntygDraftDecorator intygDraftDecorator;
    private final HsatkEmployeeService hsaEmployeeService;

    @Autowired
    public ListDraftsFacadeServiceImpl(WebCertUserService webCertUserService, UtkastService utkastService,
                                       PatientDetailsResolver patientDetailsResolver, LogService logService,
                                       IntygDraftDecorator intygDraftDecorator,
                                       HsatkEmployeeService hsaEmployeeService) {
        this.webCertUserService = webCertUserService;
        this.utkastService = utkastService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.logService = logService;
        this.intygDraftDecorator = intygDraftDecorator;
        this.hsaEmployeeService = hsaEmployeeService;
    }

    @Override
    public ListInfoDTO get(ListFilterDTO filter) {
        final var user = webCertUserService.getUser();
        final var convertedFilter = convertFilter(filter);

        var intygEntryList = getIntygEntryList(convertedFilter);
        final var totalListCount = intygEntryList.size();

        intygEntryList = decorateAndFilterOriginalList(intygEntryList, user);
        List<CertificateListItemDTO> convertedList = convertList(intygEntryList);
        sortList(convertedList, convertedFilter.getOrderBy(), convertedFilter.getOrderAscending());

        final var paginatedList = paginateList(
                convertedList, getStartFrom(filter), getPageSize(filter)
        );

        logListUsage(user, paginatedList);
        return new ListInfoDTO(totalListCount, paginatedList);
    }

    private int getPageSize(ListFilterDTO filter) {
        ListFilterNumberValueDTO pageSize = (ListFilterNumberValueDTO) filter.getValue("PAGESIZE");
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize.getValue();
    }

    private int getStartFrom(ListFilterDTO filter) {
        ListFilterNumberValueDTO startFrom = (ListFilterNumberValueDTO) filter.getValue("START_FROM");
       return startFrom == null ? 0 : startFrom.getValue();
    }

    private void logListUsage(WebCertUser user, List<CertificateListItemDTO> paginatedList) {
        paginatedList.stream().map((item) -> item.getPatientListInfo().getId()).distinct().forEach(
                id -> performPDLLogging(user, id)
        );
    }

    private List<CertificateListItemDTO> convertList(List<ListIntygEntry> intygEntryList) {
        return intygEntryList.stream().map(this::convertListItem).collect(Collectors.toList());
    }

    private List<UtkastStatus> getStatusListFromFilter(String status) {
        final var convertedStatus = DraftStatusDTO.valueOf(status);
        if(convertedStatus == DraftStatusDTO.INCOMPLETE) {
            return List.of(UtkastStatus.DRAFT_INCOMPLETE);
        } else if(convertedStatus == DraftStatusDTO.COMPLETE) {
            return List.of(UtkastStatus.DRAFT_COMPLETE);
        } else if(convertedStatus == DraftStatusDTO.LOCKED) {
            return List.of(UtkastStatus.DRAFT_LOCKED);
        }
        return Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_LOCKED, UtkastStatus.DRAFT_INCOMPLETE);
    }

    private DraftStatusDTO convertStatus(UtkastStatus status) {
        if(status == UtkastStatus.DRAFT_COMPLETE) {
            return DraftStatusDTO.COMPLETE;
        } else if(status == UtkastStatus.DRAFT_INCOMPLETE) {
            return DraftStatusDTO.INCOMPLETE;
        }
        return DraftStatusDTO.LOCKED;
    }

    private UtkastFilter convertFilter(ListFilterDTO filter) {
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();
        final var convertedFilter = new UtkastFilter(selectedUnitHsaId);

        ListFilterDateRangeValueDTO saved = (ListFilterDateRangeValueDTO) filter.getValue("SAVED");
        ListFilterSelectValueDTO savedBy = (ListFilterSelectValueDTO) filter.getValue("SAVED_BY");
        ListFilterPersonIdValueDTO patientId = (ListFilterPersonIdValueDTO) filter.getValue("PATIENT_ID");
        ListFilterSelectValueDTO forwarded = (ListFilterSelectValueDTO) filter.getValue("FORWARDED");
        ListFilterTextValueDTO orderBy = (ListFilterTextValueDTO) filter.getValue("ORDER_BY");
        ListFilterBooleanValueDTO ascending = (ListFilterBooleanValueDTO) filter.getValue("ASCENDING");
        ListFilterSelectValueDTO status = (ListFilterSelectValueDTO) filter.getValue("STATUS");


        convertedFilter.setSavedFrom(saved != null ? saved.getFrom() : null);
        convertedFilter.setSavedTo(saved != null ? saved.getTo() : null);
        convertedFilter.setSavedByHsaId(savedBy != null ? savedBy.getValue() : "");
        convertedFilter.setPatientId(patientId != null ? patientId.getValue() : "");
        convertedFilter.setNotified(forwarded != null ? getForwardedValue(forwarded.getValue()) : null);
        convertedFilter.setOrderBy(orderBy == null ? "" : orderBy.getValue());
        convertedFilter.setOrderAscending(ascending != null && ascending.getValue());
        convertedFilter.setStatusList(getStatusListFromFilter(status != null ? status.getValue() : ""));
        return convertedFilter;
    }

    private Boolean getForwardedValue(String value) {
        if(value.equals(ForwardedTypeDTO.FORWARDED.toString())) {
            return true;
        } else if(value.equals(ForwardedTypeDTO.NOT_FORWARDED.toString())) {
            return false;
        }
        return null;
    }

    private boolean isStaffAllowedToViewProtectedPatients(Personnummer patientId, String intygsTyp, WebCertUser user,
                                                          Map<Personnummer, PatientDetailsResolverResponse> sekretessStatusMap) {
        final var status = sekretessStatusMap.get(patientId).isProtectedPerson();
        if (status == SekretessStatus.UNDEFINED) {
            return false;
        } else {
            return status == SekretessStatus.FALSE || authoritiesValidator.given(user, intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified();
        }
    }

    private void markPatientStatuses(ListIntygEntry entry, PatientDetailsResolverResponse patientResponse) {
        entry.setSekretessmarkering(patientResponse.isProtectedPerson() == SekretessStatus.TRUE);
        entry.setAvliden(patientResponse.isDeceased());
        entry.setTestIntyg(patientResponse.isTestIndicator());
    }

    private List<ListIntygEntry> getIntygEntryList(UtkastFilter filter) {
        final var list = utkastService.filterIntyg(filter);
        return IntygDraftsConverter.convertUtkastsToListIntygEntries(list);
    }

    private List<ListIntygEntry> decorateAndFilterOriginalList(
            List<ListIntygEntry> listIntygEntries, WebCertUser user) {
        final var patientStatusMap = getPatientStatusMap(listIntygEntries);

        listIntygEntries = filterEntriesForProtectedPatients(user, listIntygEntries, patientStatusMap);
        listIntygEntries.forEach(entry -> markPatientStatuses(entry, patientStatusMap.get(entry.getPatientId())));

        intygDraftDecorator.decorateWithCertificateTypeName(listIntygEntries);
        decorateWithDoctorName(listIntygEntries);

        return listIntygEntries;
    }

    private void decorateWithDoctorName(List<ListIntygEntry> list) {
        final var hsaIds = list.stream().map(ListIntygEntry::getUpdatedSignedById).collect(Collectors.toSet());
        final var hsaIdNameMap = getNamesByHsaIds(hsaIds);

        list.forEach(entry -> {
            if (hsaIdNameMap.containsKey(entry.getUpdatedSignedById())) {
                entry.setUpdatedSignedBy(hsaIdNameMap.get(entry.getUpdatedSignedById()));
            }
        });

    }

    private CertificateListItemDTO convertListItem(ListIntygEntry listIntygEntry) {
        final var listItem = new CertificateListItemDTO();
        final var convertedStatus = convertStatus(UtkastStatus.fromValue(listIntygEntry.getStatus()));
        final var patientListInfo = new PatientListInfoDTO(listIntygEntry.getPatientId().getPersonnummerWithDash(),
                listIntygEntry.isSekretessmarkering(), listIntygEntry.isAvliden(), listIntygEntry.isTestIntyg());
        listItem.setCertificateId(listIntygEntry.getIntygId());
        listItem.setCertificateType(listIntygEntry.getIntygType());
        listItem.setForwarded(listIntygEntry.isVidarebefordrad());
        listItem.setPatientListInfo(patientListInfo);
        listItem.setStatus(convertedStatus.getName());
        listItem.setSaved(listIntygEntry.getLastUpdatedSigned());
        listItem.setSavedBy(listIntygEntry.getUpdatedSignedBy());
        listItem.setCertificateTypeName(listIntygEntry.getIntygTypeName());
        return listItem;
    }

    private Comparator<CertificateListItemDTO> getCertificateComparator(ListColumnTypeDTO orderBy, Boolean ascending) {
        Comparator<CertificateListItemDTO> comparator;
        switch (orderBy) {
            case CERTIFICATE_TYPE_NAME:
                comparator = Comparator.comparing(CertificateListItemDTO::getCertificateTypeName);
                break;
            case STATUS:
                comparator = Comparator.comparing(CertificateListItemDTO::getStatus);
                break;
            case PATIENT_ID:
                comparator = Comparator.comparing(item -> item.getPatientListInfo().getId());
                break;
            case FORWARDED:
                comparator = (item1, item2) -> Boolean.compare(item1.isForwarded(), item2.isForwarded());
                break;
            case SAVED_BY:
                comparator = Comparator.comparing((CertificateListItemDTO::getSavedBy));
                break;
            case SAVED:
            default:
                comparator = Comparator.comparing(CertificateListItemDTO::getSaved);
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private void sortList(List<CertificateListItemDTO> list, String orderBy, boolean isAscending) {
        final var comparator = getCertificateComparator(ListColumnTypeDTO.valueOf(orderBy), isAscending);
        list.sort(comparator);
    }

    private List<CertificateListItemDTO> paginateList(List<CertificateListItemDTO> list, int startFrom, int pageSize) {
        if (startFrom < list.size()) {
            int toIndex = startFrom + pageSize;
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            return list.subList(startFrom, toIndex);
        } else {
            return Collections.emptyList();
        }
    }

    private void performPDLLogging(WebCertUser user, String patientId) {
        logService.logListIntyg(user, patientId); //with dash???
    }

    private List<ListIntygEntry> filterEntriesForProtectedPatients(
            WebCertUser user, List<ListIntygEntry> listIntygEntries, Map<Personnummer, PatientDetailsResolverResponse> patientStatusMap) {
        listIntygEntries = listIntygEntries.stream()
                .filter(
                        entry -> this.isStaffAllowedToViewProtectedPatients(
                                entry.getPatientId(), entry.getIntygType(), user, patientStatusMap
                        )
                )
                .collect(Collectors.toList());
        return listIntygEntries;
    }

    private Map<Personnummer, PatientDetailsResolverResponse> getPatientStatusMap(
            List<ListIntygEntry> listIntygEntries) {
        return patientDetailsResolver.getPersonStatusesForList(listIntygEntries.stream()
                .map(ListIntygEntry::getPatientId)
                .collect(Collectors.toList()));
    }

    Map<String, String> getNamesByHsaIds(Set<String> hsaIds) {
        return ArendeConverter.getNamesByHsaIds(hsaIds, hsaEmployeeService);
    }
}
