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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResultCode.NOT_AUTHORIZED_IN_HOSP;
import static se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResultCode.NO_ACCOUNT;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResultCode;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
@RequiredArgsConstructor
public class GetUserResourceLinksImpl implements GetUserResourceLinks {

    @Nullable
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;

    @Override
    public ResourceLinkDTO[] get(WebCertUser user) {
        final var availableFunctions = new ArrayList<>(getAvailableFunctionsForUser(user));
        return availableFunctions.toArray(ResourceLinkDTO[]::new);
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForUser(WebCertUser user) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        if (hasAccessToSearchCreatePage(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE,
                    "Sök / skriv intyg",
                    "",
                    true
                )
            );
        }

        if (hasAccessToDraftList(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_DRAFT_LIST,
                    "Ej signerade utkast",
                    "",
                    true
                )
            );
        }

        if (hasAccessToQuestionList(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_QUESTION_LIST,
                    "Ej hanterade ärenden",
                    "",
                    true
                )
            );
        }

        if (hasAccessToSignedCertificatesList(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST,
                    "Signerade intyg",
                    "",
                    true
                )
            );
        }

        if (isLogOutAvailable(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.LOG_OUT,
                    "Logga ut",
                    "",
                    true
                )
            );
        }

        if (isChooseUnitAvailable(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CHOOSE_UNIT,
                    "Välj vårdenhet",
                    "",
                    true
                )
            );
        }

        if (isChangeUnitAvailable(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CHANGE_UNIT,
                    "Byt vårdenhet",
                    "",
                    true
                )
            );
        }

        if (hasNavigateBackButton(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.NAVIGATE_BACK_BUTTON,
                    "Tillbaka",
                    "",
                    true
                )
            );
        }

        if (hasNormalOriginWarning(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.WARNING_NORMAL_ORIGIN,
                    "Felaktig inloggningsmetod",
                    "",
                    "",
                    true
                )
            );
        }

        if (shouldWarnForMissingSubscription(user)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SUBSCRIPTION_WARNING,
                    "Saknar avtal",
                    "",
                    true
                )
            );
        }

        addAvailableFunctionsForPrivatePractitioner(user, resourceLinks);

        return resourceLinks;
    }

    private void addAvailableFunctionsForPrivatePractitioner(WebCertUser user, ArrayList<ResourceLinkDTO> resourceLinks) {
        final var resultCode = privatePractitionerValidationResultCode(user);
        if (hasAccessToRegisterPrivatePractitioner(user, resultCode)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER,
                    "Skapa konto i Webcert",
                    "",
                    true
                )
            );
        }

        if (hasAccessToEditPrivatePractitioner(user, resultCode)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ACCESS_EDIT_PRIVATE_PRACTITIONER,
                    "Ändra uppgifter",
                    "",
                    true
                )
            );
        }

        if (notAuthorizedPrivatePractitioner(user, resultCode)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.NOT_AUTHORIZED_PRIVATE_PRACTITIONER,
                    "Du är inte behörig att använda Webcert",
                    "",
                    true
                )
            );
        }
    }

    private boolean hasNormalOriginWarning(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) && user.isFeatureActive("VARNING_FRISTAENDE") && hasUserChosenUnit(user);
    }

    private boolean hasUserChosenUnit(WebCertUser user) {
        return user.getValdVardenhet() != null;
    }

    private boolean isChooseUnitAvailable(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) && !hasUserChosenUnit(user);
    }

    private boolean isChangeUnitAvailable(WebCertUser user) {
        return hasMoreThanOneUnitOrSubUnit(user) && (isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin()));
    }

    private boolean hasMoreThanOneUnitOrSubUnit(WebCertUser user) {
        if (user.getVardgivare().size() > 1) {
            return true;
        }
        for (Vardgivare vg : user.getVardgivare()) {
            if (vg.getVardenheter().size() > 1) {
                return true;
            }
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getMottagningar().size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAccessToSearchCreatePage(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToDraftList(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToQuestionList(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasNavigateBackButton(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin());
    }

    private boolean hasAccessToSignedCertificatesList(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return (isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin())) && !isUserCareAdmin(user);
    }

    private boolean isLogOutAvailable(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean isOriginNormal(String origin) {
        return "NORMAL".equals(origin);
    }

    private boolean isOriginUthopp(String origin) {
        return "UTHOPP".equals(origin);
    }

    private boolean isUserCareAdmin(WebCertUser user) {
        return user.getRoles().containsKey(AuthoritiesConstants.ROLE_ADMIN);
    }

    private boolean shouldWarnForMissingSubscription(WebCertUser user) {
        if (user.isUnauthorizedPrivatePractitioner()) {
            return false;
        }
        return isOriginNormal(user.getOrigin()) && isLoggedInCareProviderMissingSubscription(user);
    }

    private boolean isLoggedInCareProviderMissingSubscription(WebCertUser user) {
        return user.getValdVardgivare() != null
            && user.getSubscriptionInfo().getCareProvidersForSubscriptionModal() != null
            && user.getSubscriptionInfo().getCareProvidersForSubscriptionModal().contains(user.getValdVardgivare().getId());
    }

    private PrivatePractitionerValidationResultCode privatePractitionerValidationResultCode(WebCertUser user) {
        if (privatePractitionerIntegrationService == null) {
            return null;
        }
        if (user.isUnauthorizedPrivatePractitioner() || user.isPrivatLakare()) {
            return privatePractitionerIntegrationService.validatePrivatePractitioner(user.getPersonId()).resultCode();
        }
        return null;
    }

    private boolean hasAccessToEditPrivatePractitioner(WebCertUser user, PrivatePractitionerValidationResultCode resultCode) {
        return user.isUnauthorizedPrivatePractitioner() && resultCode == NOT_AUTHORIZED_IN_HOSP || user.isPrivatLakare();
    }

    private boolean hasAccessToRegisterPrivatePractitioner(WebCertUser user, PrivatePractitionerValidationResultCode resultCode) {
        return user.isUnauthorizedPrivatePractitioner() && resultCode == NO_ACCOUNT;
    }

    private boolean notAuthorizedPrivatePractitioner(WebCertUser user, PrivatePractitionerValidationResultCode resultCode) {
        return user.isUnauthorizedPrivatePractitioner() && resultCode != NO_ACCOUNT;
    }
}
