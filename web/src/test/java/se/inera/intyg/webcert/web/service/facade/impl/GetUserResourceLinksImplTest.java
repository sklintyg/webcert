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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetUserResourceLinksImplTest {

    @Nested
    class ResourceLinks {

        @InjectMocks
        private GetUserResourceLinksImpl getUserResourceLinks;

        WebCertUser getUser(String origin) {
            final var user = new WebCertUser();
            user.setOrigin(origin);
            return user;
        }

        @Test
        void shallIncludeLogoutIfOriginIsNormal() {
            final var user = getUser("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallNotIncludeLogoutIfOriginIsNotNormal() {
            final var user = getUser("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallIncludeCreateCertificateIfOriginIsNormal() {
            final var user = getUser("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE);
        }

        @Test
        void shallNotIncludeCreateCertificateIfOriginIsNotNormal() {
            final var user = getUser("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE);
        }
    }
}

