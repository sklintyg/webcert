/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import jakarta.xml.ws.WebServiceException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.util.UtkastUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Service
public class ListDecoratorImpl implements ListDecorator {

    private final IntygDraftDecorator intygDraftDecorator;
    private final HsatkEmployeeService hsaEmployeeService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    private final WebCertUserService webCertUserService;
    private final ResourceLinkHelper resourceLinkHelper;
    private final DraftAccessServiceHelper draftAccessServiceHelper;


    public ListDecoratorImpl(IntygDraftDecorator intygDraftDecorator, HsatkEmployeeService hsaEmployeeService,
        PatientDetailsResolver patientDetailsResolver, WebCertUserService webCertUserService,
        ResourceLinkHelper resourceLinkHelper, DraftAccessServiceHelper draftAccessServiceHelper) {
        this.intygDraftDecorator = intygDraftDecorator;
        this.hsaEmployeeService = hsaEmployeeService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.webCertUserService = webCertUserService;
        this.resourceLinkHelper = resourceLinkHelper;
        this.draftAccessServiceHelper = draftAccessServiceHelper;
    }

    @Override
    public void decorateWithCertificateTypeName(List<ListIntygEntry> list) {
        intygDraftDecorator.decorateWithCertificateTypeName(list);
    }

    @Override
    public void decorateWithStaffName(List<ListIntygEntry> list) {
        final var hsaIds = list.stream().map(ListIntygEntry::getUpdatedSignedById).collect(Collectors.toSet());
        final var hsaIdNameMap = getNamesByHsaIds(hsaIds);

        list.forEach(entry -> {
            if (hsaIdNameMap.containsKey(entry.getUpdatedSignedById())) {
                entry.setUpdatedSignedBy(hsaIdNameMap.get(entry.getUpdatedSignedById()));
            }
        });
    }

    @Override
    public void decorateWithResourceLinks(List<ListIntygEntry> list) {
        list.forEach(entry -> {
            resourceLinkHelper.decorateIntygWithValidActionLinks(entry, entry.getPatientId());
            decorateWithForwardLink(entry);
        });
    }

    private void decorateWithForwardLink(ListIntygEntry entry) {
        final var isForwardingAllowed = draftAccessServiceHelper.isAllowedToForwardUtkast(
            AccessEvaluationParameters.create(
                entry.getIntygType(),
                entry.getIntygTypeVersion(),
                UtkastUtil.getCareUnit(entry.getVardgivarId(), entry.getVardenhetId()),
                entry.getPatientId(),
                entry.isTestIntyg()
            )
        );

        if (isForwardingAllowed) {
            entry.addLink(new ActionLink(ActionLinkType.VIDAREBEFORDRA_UTKAST));
        }
    }

    private Map<String, String> getNamesByHsaIds(Collection<String> hsaIds) {
        Map<String, String> hsaIdNameMap = new HashMap<>();

        hsaIds.forEach(hsaId -> {
            Optional<String> name = getNameByHsaIdNullIfNotFound(hsaId);
            name.ifPresent(s -> hsaIdNameMap.put(hsaId, s));
        });

        return hsaIdNameMap;
    }

    private Optional<String> getNameByHsaIdNullIfNotFound(String hsaId) {
        try {
            return Optional.of(getNameByHsaId(hsaId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getNameByHsaId(String hsaId) {
        try {
            return hsaEmployeeService.getEmployee(null, hsaId)
                .stream()
                .filter(this::isMiddleAndLastNameDefined)
                .map(this::getName)
                .findFirst()
                .orElseThrow(
                    () -> new WebCertServiceException(
                        WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No name was found in HSA")
                );
        } catch (WebServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                "Could not communicate with HSA. Cause: " + e.getMessage());
        }
    }

    private boolean isMiddleAndLastNameDefined(PersonInformation personInformation) {
        return !Strings.isNullOrEmpty(personInformation.getMiddleAndSurName());
    }

    private boolean isFirstNameDefined(PersonInformation personInformation) {
        return !Strings.isNullOrEmpty(personInformation.getGivenName());
    }

    private String getName(PersonInformation personInformation) {
        return !isFirstNameDefined(personInformation) ? personInformation.getMiddleAndSurName()
            : personInformation.getGivenName() + " " + personInformation.getMiddleAndSurName();

    }

    @Override
    public List<ListIntygEntry> decorateAndFilterProtectedPerson(List<ListIntygEntry> list) {
        final var user = webCertUserService.getUser();
        final var patientStatusMap = getPatientStatusMap(list);

        list = filterEntriesForProtectedPatients(user, list, patientStatusMap);
        list.forEach(entry -> markPatientStatuses(entry, patientStatusMap.get(entry.getPatientId())));

        return list;
    }

    private Map<Personnummer, PatientDetailsResolverResponse> getPatientStatusMap(List<ListIntygEntry> listIntygEntries) {
        return patientDetailsResolver.getPersonStatusesForList(
            listIntygEntries.stream()
                .map(ListIntygEntry::getPatientId)
                .collect(Collectors.toList())
        );
    }

    private List<ListIntygEntry> filterEntriesForProtectedPatients(
        WebCertUser user, List<ListIntygEntry> listIntygEntries, Map<Personnummer,
        PatientDetailsResolverResponse> patientStatusMap) {
        listIntygEntries = listIntygEntries.stream()
            .filter(
                entry -> this.isStaffAllowedToViewProtectedPatients(
                    entry.getPatientId(), entry.getIntygType(), user, patientStatusMap
                )
            )
            .collect(Collectors.toList());
        return listIntygEntries;
    }

    private void markPatientStatuses(ListIntygEntry entry, PatientDetailsResolverResponse patientResponse) {
        entry.setSekretessmarkering(patientResponse.isProtectedPerson() == SekretessStatus.TRUE);
        entry.setAvliden(patientResponse.isDeceased());
        entry.setTestIntyg(patientResponse.isTestIndicator());
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
}
