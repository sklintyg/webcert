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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.util.PatientToolkit;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.MissingRelatedCertificateConfirmation;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.modal.confirmation.ConfirmationModalProviderResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Service("getCertificateTypeInfoFromWebcert")
public class GetCertificateTypesFacadeServiceImpl implements GetCertificateTypesFacadeService {

    private final IntygModuleRegistry intygModuleRegistry;
    private final ResourceLinkHelper resourceLinkHelper;
    private final AuthoritiesHelper authoritiesHelper;
    private final WebCertUserService webCertUserService;
    private final IntygTextsService intygTextsService;
    private final CertificateTypeMessageService certificateTypeMessageService;
    private final PatientDetailsResolver patientDetailsResolver;
    private final MissingRelatedCertificateConfirmation missingRelatedCertificateConfirmation;
    private final FeaturesHelper featuresHelper;

    @Autowired
    public GetCertificateTypesFacadeServiceImpl(IntygModuleRegistry intygModuleRegistry, ResourceLinkHelper resourceLinkHelper,
        AuthoritiesHelper authoritiesHelper, WebCertUserService webCertUserService,
        IntygTextsService intygTextsService, CertificateTypeMessageService certificateTypeMessageService,
        PatientDetailsResolver patientDetailsResolver,
        MissingRelatedCertificateConfirmation missingRelatedCertificateConfirmation, FeaturesHelper featuresHelper) {
        this.intygModuleRegistry = intygModuleRegistry;
        this.resourceLinkHelper = resourceLinkHelper;
        this.authoritiesHelper = authoritiesHelper;
        this.webCertUserService = webCertUserService;
        this.intygTextsService = intygTextsService;
        this.certificateTypeMessageService = certificateTypeMessageService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.missingRelatedCertificateConfirmation = missingRelatedCertificateConfirmation;
        this.featuresHelper = featuresHelper;
    }

    @Override
    public List<CertificateTypeInfoDTO> get(Personnummer patientId) {
        final var certificateModuleList = getCertificateModuleList(patientId);
        return certificateModuleList.stream()
            .map(module -> convertModuleToTypeInfo(module, patientId))
            .map(certificateTypeInfoDTO -> addAdditionalResourceLinks(certificateTypeInfoDTO, patientId))
            .map(certificateTypeInfoDTO -> addConfirmationModal(certificateTypeInfoDTO, patientId))
            .toList();
    }

    private CertificateTypeInfoDTO addAdditionalResourceLinks(CertificateTypeInfoDTO intygModule, Personnummer patientId) {
        if (intygModule.getId().equals(LuaenaEntryPoint.MODULE_ID) && patientOlderThanThirtyYearsAndTwoMonths(patientId)) {
            intygModule.getLinks().add(ResourceLinkFactory.confirmLuaena(true));
        }
        missingRelatedCertificateConfirmation.get(intygModule.getId(), patientId)
            .ifPresent(resourceLinkDTO -> intygModule.getLinks().add(resourceLinkDTO));
        return intygModule;
    }

    private CertificateTypeInfoDTO addConfirmationModal(CertificateTypeInfoDTO intygModule, Personnummer patientId) {
        final var isAllowedToEdit = intygModule.getLinks().stream()
            .anyMatch(link -> link.getType() == ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        final var provider = ConfirmationModalProviderResolver.getConfirmation(intygModule.getId(), CertificateStatus.UNSIGNED,
            webCertUserService.getUser(), true, isAllowedToEdit);
        if (provider != null) {
            final var latestCertificateVersion = intygTextsService.getLatestVersion(intygModule.getId());
            final var patient = patientDetailsResolver.resolvePatient(patientId, intygModule.getId(), latestCertificateVersion);
            intygModule.setConfirmationModal(
                provider.create(patient.getFullstandigtNamn(), patientId.getPersonnummerWithDash(),
                    webCertUserService.getUser().getOrigin())
            );
        }

        return intygModule;
    }

    private CertificateTypeInfoDTO convertModuleToTypeInfo(IntygModuleDTO module, Personnummer patientId) {
        final var certificateTypeInfo = new CertificateTypeInfoDTO();
        certificateTypeInfo.setId(module.getId());
        certificateTypeInfo.setCertificateServiceTypeId(module.getCertificateServiceTypeId());
        certificateTypeInfo.setLabel(module.getLabel());
        certificateTypeInfo.setDescription(module.getDescription());
        certificateTypeInfo.setDetailedDescription(module.getDetailedDescription());
        certificateTypeInfo.setIssuerTypeId(module.getIssuerTypeId());
        certificateTypeInfo.setLinks(convertResourceLinks(module.getLinks()));
        certificateTypeMessageService.get(module.getId(), patientId)
            .ifPresent(message -> certificateTypeInfo.setMessage(message.getMessage()));
        return certificateTypeInfo;
    }

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links) {
        final var list = links.stream()
            .map(this::convertResourceLink)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (list.stream().noneMatch(link -> link.getType() == ResourceLinkTypeDTO.CREATE_CERTIFICATE)) {
            list.add(ResourceLinkFactory.create(false));
        }

        return list;
    }

    private ResourceLinkDTO convertResourceLink(ActionLink link) {
        if (link.getType() == ActionLinkType.SKAPA_UTKAST) {
            return ResourceLinkFactory.create(true);
        }
        return null;
    }

    private List<IntygModuleDTO> getCertificateModuleList(Personnummer personnummer) {
        final var intygModules = intygModuleRegistry.listAllModules();
        final var allowedCertificateTypes = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
            AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);
        final var inactiveCertificateTypes = featuresHelper.getCertificateTypesForFeature(
            AuthoritiesConstants.FEATURE_INACTIVE_CERTIFICATE_TYPE);

        final var intygModuleDTOs = intygModules.stream()
            .map(IntygModuleDTO::new)
            .filter(intygModuleDTO -> !inactiveCertificateTypes.contains(intygModuleDTO.getId()))
            .filter(intygModule -> allowedCertificateTypes.contains(intygModule.getId()))
            .filter(intygModule -> intygTextsService.getLatestVersion(intygModule.getId()) != null)
            .toList();

        resourceLinkHelper.decorateIntygModuleWithValidActionLinks(intygModuleDTOs, personnummer);

        return intygModuleDTOs;
    }

    private boolean patientOlderThanThirtyYearsAndTwoMonths(Personnummer patientId) {
        final var birthDate = PatientToolkit.birthDate(patientId);
        return LocalDate.now(ZoneId.systemDefault()).isAfter(birthDate.plusYears(30).plusMonths(2));
    }
}
