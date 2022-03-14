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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GetUserResourceLinksImplTest {

    @Mock
    WebCertUserService webCertUserService;

    @InjectMocks
    private GetUserResourceLinksImpl getUserResourceLinks;

    @Nested
    class ResourceLinks {

        void setup(String origin) {
            final var user = new WebCertUser();
            user.setOrigin(origin);
            doReturn(user).when(webCertUserService).getUser();
        }

        @Test
        void shallIncludeLogoutIfOriginIsNormal() {
            setup("NORMAL");
            final var actualLinks = getUserResourceLinks.get();
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallNotIncludeLogoutIfOriginIsNotNormal() {
            setup("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get();
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallIncludeCreateCertificateIfOriginIsNormal() {
            setup("NORMAL");
            final var actualLinks = getUserResourceLinks.get();
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.CREATE_CERTIFICATE);
        }

        @Test
        void shallNotIncludeCreateCertificateIfOriginIsNotNormal() {
            setup("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get();
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.CREATE_CERTIFICATE);
        }
    }
}

