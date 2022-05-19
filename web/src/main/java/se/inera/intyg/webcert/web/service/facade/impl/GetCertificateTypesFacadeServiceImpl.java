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
package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Service
public class GetCertificateTypesFacadeServiceImpl implements GetCertificateTypesFacadeService {

    private final IntygModuleRegistry intygModuleRegistry;
    private final ResourceLinkHelper resourceLinkHelper;

    @Autowired
    public GetCertificateTypesFacadeServiceImpl(IntygModuleRegistry intygModuleRegistry, ResourceLinkHelper resourceLinkHelper) {
        this.intygModuleRegistry = intygModuleRegistry;
        this.resourceLinkHelper = resourceLinkHelper;
    }

    @Override
    public List<CertificateTypeInfoDTO> get(String patientId) throws InvalidPersonNummerException {
        final var personnummer = createPnr(patientId);
        final var certificateModuleList = getCertificateModuleList(personnummer);
        return certificateModuleList.stream().map(this::convertModuleToTypeInfo).collect(Collectors.toList());
    }

    private CertificateTypeInfoDTO convertModuleToTypeInfo(IntygModuleDTO module) {
        final var certificateTypeInfo = new CertificateTypeInfoDTO();
        certificateTypeInfo.setId(module.getId());
        certificateTypeInfo.setLabel(module.getLabel());
        certificateTypeInfo.setDescription(module.getDescription());
        certificateTypeInfo.setDetailedDescription(module.getDetailedDescription());
        certificateTypeInfo.setIssuerTypeId(module.getIssuerTypeId());
        certificateTypeInfo.setLinks(convertResourceLinks(module.getLinks()));
        return certificateTypeInfo;
    }

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links) {
        final var list = links.stream()
                .map(this::convertResourceLink)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(list.stream().noneMatch(link -> link.getType() == ResourceLinkTypeDTO.CREATE_CERTIFICATE)) {
            list.add(ResourceLinkDTO.create(ResourceLinkTypeDTO.CREATE_CERTIFICATE, "Skapa intyg", "", false));
        }

        return list;
    }

    private ResourceLinkDTO convertResourceLink(ActionLink link) {
        if (link.getType() == ActionLinkType.SKAPA_UTKAST) {
            return ResourceLinkDTO.create(ResourceLinkTypeDTO.CREATE_CERTIFICATE, "Skapa intyg", "Skapa ett intygsutkast.", true);
        }
        return null;
    }

    private List<IntygModuleDTO> getCertificateModuleList(Personnummer personnummer) {
        final var intygModules = intygModuleRegistry.listAllModules();

        final var intygModuleDTOs = intygModules.stream()
            .map(IntygModuleDTO::new)
            .filter((intygModule) -> intygModule.isDisplayDeprecated() || !intygModule.isDeprecated())
            .collect(Collectors.toList());

        resourceLinkHelper.decorateIntygModuleWithValidActionLinks(intygModuleDTOs, personnummer);

        return intygModuleDTOs;
    }

    private Personnummer createPnr(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
    }
}
