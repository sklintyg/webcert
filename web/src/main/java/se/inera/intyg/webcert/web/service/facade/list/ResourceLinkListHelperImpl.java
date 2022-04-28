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
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.UserService;
import se.inera.intyg.webcert.web.service.facade.impl.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResourceLinkListHelperImpl implements ResourceLinkListHelper {

    private final CertificateAccessServiceHelper certificateAccessServiceHelper;
    private final WebCertUserService webCertUserService;
    private final UserService userService;

    @Autowired
    public ResourceLinkListHelperImpl(CertificateAccessServiceHelper certificateAccessServiceHelper,
                                      WebCertUserService webCertUserService, UserService userService) {
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
        this.webCertUserService = webCertUserService;
        this.userService = userService;
    }

    @Override
    public List<ResourceLinkDTO> get(CertificateListEntry entry, String status) {
        final var actionLinks = getActionLinks(entry);
        return convertResourceLinks(actionLinks, status);
    }

    @Override
    public List<ResourceLinkDTO> get(ListIntygEntry entry, String status) {
        return convertResourceLinks(entry.getLinks(), entry.getVardenhetId(), entry.getIntygType(), status);
    }

    private List<ActionLink> getActionLinks(CertificateListEntry entry) {
        final var user = webCertUserService.getUser();
        final var careUnit = createCareUnit(user.getValdVardenhet().getId(), user.getValdVardgivare().getId());
        final var links = new ArrayList<ActionLink>();

        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
                entry.getCertificateType(),
                entry.getCertificateTypeVersion(),
                careUnit,
                Personnummer.createPersonnummer(entry.getCivicRegistrationNumber()).get(),
                entry.isTestIndicator()
        );

        if (certificateAccessServiceHelper.isAllowToRead(accessEvaluationParameters)) {
            links.add(new ActionLink(ActionLinkType.LASA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            links.add(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }

        return links;
    }

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links, String status) {
        return links.stream()
                .map((link) -> getConvertedResourceLink(link, status))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links, String unitId, String certificateType, String status) {
        return links.stream()
                .map((link) -> getConvertedResourceLink(link, unitId, certificateType, status))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ResourceLinkDTO getConvertedResourceLink(ActionLink link, String status) {
        if (link.getType() == ActionLinkType.LASA_INTYG) {
            return ResourceLinkFactory.read();
        } else if (link.getType() == ActionLinkType.VIDAREBEFORDRA_UTKAST && !status.equals(UtkastStatus.DRAFT_LOCKED.toString())) {
            return ResourceLinkFactory.forwardGeneric();
        }
        return null;
    }

    private ResourceLinkDTO getConvertedResourceLink(ActionLink link, String savedUnitId, String certificateType, String status) {
        final var convertedResourceLink = getConvertedResourceLink(link, status);
        if (convertedResourceLink != null) {
            return convertedResourceLink;
        } else if (link.getType() == ActionLinkType.FORNYA_INTYG) {
            final var loggedInCareUnitId = userService.getLoggedInCareUnit(webCertUserService.getUser()).getId();
            return ResourceLinkFactory.renew(loggedInCareUnitId, savedUnitId, certificateType);
        }
        return null;
    }

    private Vardenhet createCareUnit(String unitId, String caregiverId) {
        final var vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(unitId);
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(caregiverId);
        return vardenhet;
    }
}
