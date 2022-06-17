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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetUserResourceLinksImpl implements GetUserResourceLinks {

    @Autowired
    public GetUserResourceLinksImpl() {
    }

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

        if (hasAccessToDraftList(user)) {
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

        return resourceLinks;
    }

    private boolean isChooseUnitAvailable(WebCertUser user) {
        final var loggedInUnit =  user.getValdVardenhet();
        return isOriginNormal(user.getOrigin()) && loggedInUnit == null;
    }

    private boolean isChangeUnitAvailable(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToSearchCreatePage(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToDraftList(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToQuestionList(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean hasAccessToSignedCertificatesList(WebCertUser user) {
        return (isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin())) && isUserDoctor(user);
    }

    private boolean isLogOutAvailable(WebCertUser user) {
        return isOriginNormal(user.getOrigin()) || isOriginUthopp(user.getOrigin());
    }

    private boolean isOriginNormal(String origin) {
        return origin.equals("NORMAL");
    }

    private boolean isOriginUthopp(String origin) {
        return origin.equals("UTHOPP");
    }

    private boolean isUserDoctor(WebCertUser user) {
        return user.isPrivatLakare() || user.isLakare();
    }
}
