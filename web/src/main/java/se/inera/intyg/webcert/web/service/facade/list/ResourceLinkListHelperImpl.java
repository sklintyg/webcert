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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateForwardFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateRenewFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.facade.util.CertificateRelationsConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Service
public class ResourceLinkListHelperImpl implements ResourceLinkListHelper {

    private final CertificateAccessServiceHelper certificateAccessServiceHelper;
    private final WebCertUserService webCertUserService;
    private final UserService userService;
    private final CertificateRelationsConverter certificateRelationsConverter;
    private final AuthoritiesHelper authoritiesHelper;

    @Autowired
    public ResourceLinkListHelperImpl(CertificateAccessServiceHelper certificateAccessServiceHelper,
        WebCertUserService webCertUserService, UserService userService,
        CertificateRelationsConverter certificateRelationsConverter,
        AuthoritiesHelper authoritiesHelper) {
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
        this.webCertUserService = webCertUserService;
        this.userService = userService;
        this.certificateRelationsConverter = certificateRelationsConverter;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public List<ResourceLinkDTO> get(CertificateListEntry entry, CertificateListItemStatus status) {
        final var actionLinks = getActionLinks(entry);
        return convertResourceLinks(actionLinks, status);
    }

    @Override
    public List<ResourceLinkDTO> get(ListIntygEntry entry, CertificateListItemStatus status) {
        final var convertedRelations = certificateRelationsConverter.convert(entry.getRelations());
        return convertResourceLinks(entry.getLinks(), entry.getVardenhetId(), entry.getIntygType(), status, convertedRelations);
    }

    @Override
    public List<ResourceLinkDTO> get(ArendeListItem entry, CertificateListItemStatus status) {
        return convertResourceLinks(entry.getLinks(), status);
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

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links, CertificateListItemStatus status) {
        return links.stream()
            .map((link) -> getConvertedResourceLink(link, status))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<ResourceLinkDTO> convertResourceLinks(
        List<ActionLink> links, String unitId, String certificateType,
        CertificateListItemStatus status, CertificateRelations relations
    ) {
        return links.stream()
            .map((link) -> getConvertedResourceLink(link, unitId, certificateType, status, relations))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private ResourceLinkDTO getConvertedResourceLink(ActionLink link, CertificateListItemStatus status) {
        if (link.getType() == ActionLinkType.LASA_INTYG || link.getType() == ActionLinkType.LASA_FRAGA) {
            return ResourceLinkFactory.read();
        }

        if (link.getType() == ActionLinkType.VIDAREBEFODRA_FRAGA) {
            return CertificateForwardFunction.createResourceLinkForQuestionList();
        }

        if (validateForward(link, getCertificateStatus(status))) {
            return CertificateForwardFunction.createResourceLink();
        }

        return null;
    }

    private CertificateStatus getCertificateStatus(CertificateListItemStatus status) {
        if (status == CertificateListItemStatus.LOCKED) {
            return CertificateStatus.LOCKED;
        } else if (status == CertificateListItemStatus.INCOMPLETE || status == CertificateListItemStatus.COMPLETE) {
            return CertificateStatus.UNSIGNED;
        } else if (status == CertificateListItemStatus.REVOKED) {
            return CertificateStatus.REVOKED;
        } else {
            return CertificateStatus.SIGNED;
        }
    }

    private boolean validateForward(ActionLink link, CertificateStatus status) {
        return link.getType() == ActionLinkType.VIDAREBEFORDRA_UTKAST
            && CertificateForwardFunction.validate(status, webCertUserService.getUser());
    }

    private ResourceLinkDTO getConvertedResourceLink(
        ActionLink link, String savedUnitId, String certificateType,
        CertificateListItemStatus status, CertificateRelations relations
    ) {
        final var convertedResourceLink = getConvertedResourceLink(link, status);
        if (convertedResourceLink != null) {
            return convertedResourceLink;
        } else if (validateRenew(link, certificateType, relations, getCertificateStatus(status))) {
            final var loggedInCareUnitId = userService.getLoggedInCareUnit(webCertUserService.getUser()).getId();
            return CertificateRenewFunction.createResourceLink(loggedInCareUnitId, savedUnitId, certificateType);
        }
        return null;
    }

    private boolean validateRenew(ActionLink link, String certificateType, CertificateRelations relations, CertificateStatus status) {
        if (link.getType() == ActionLinkType.FORNYA_INTYG_FRAN_CERTIFICATE_SERVICE) {
            return true;
        }
        return link.getType() == ActionLinkType.FORNYA_INTYG
            && CertificateRenewFunction.validate(certificateType, relations, status, authoritiesHelper);
    }

    private Vardenhet createCareUnit(String unitId, String caregiverId) {
        final var vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(unitId);
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(caregiverId);
        return vardenhet;
    }
}
