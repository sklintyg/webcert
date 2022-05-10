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
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetUserResourceLinksImpl;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsServiceImpl;
import se.inera.intyg.webcert.web.service.facade.user.UserTabFactory;
import se.inera.intyg.webcert.web.service.facade.user.UserTabsServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserTabsServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private GetUserResourceLinksImpl getUserResourceLinks;

    @Mock
    private UserStatisticsServiceImpl userStatisticsService;

    @InjectMocks
    private UserTabsServiceImpl userTabsService;

    private WebCertUser user;

    void setUpUser() {
        user = mock(WebCertUser.class);

       doReturn(user)
            .when(webCertUserService)
            .getUser();
    }

    void setupDoctor() {
        setUpUser();
        doReturn(true)
                .when(user)
                .isLakare();
    }

    void setupAdmin() {
        setUpUser();
        doReturn(false)
                .when(user)
                .isLakare();
    }

    void setupAllResourceLinks() {
        final var links = new ResourceLinkDTO[]{
                getResourceLink(ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE),
                getResourceLink(ResourceLinkTypeDTO.ACCESS_DRAFT_LIST),
                getResourceLink(ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST)
        };
        doReturn(links).when(getUserResourceLinks).get(any());
    }

    void setupNoResourceLinks() {
        doReturn(new ResourceLinkDTO[0]).when(getUserResourceLinks).get(any());
    }

    @Nested
    class GetTabsForDoctor {
        @Test
        void shouldSetAllTabsIfLinksExist() {
            setupDoctor();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(3, result.size());
        }

        @Test
        void shouldSetSearchAndCreateTabFirst() {
            setupDoctor();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(UserTabFactory.searchCreate().getTitle(), result.get(0).getTitle());
        }

        @Test
        void shouldSetDraftsList() {
            setupDoctor();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(UserTabFactory.listDrafts(0).getTitle(), result.get(1).getTitle());
        }

        @Test
        void shouldSetSignedCertificates() {
            setupDoctor();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(UserTabFactory.signedCertificates().getTitle(), result.get(2).getTitle());
        }

        @Test
        void shouldSetNoTabsIfNoLinks() {
            setupDoctor();
            setupNoResourceLinks();

            final var result = userTabsService.get();

            assertEquals(0, result.size());
        }
    }

    @Nested
    class GetTabsForAdmin {
        @Test
        void shouldSetTabsIfLinksExist() {
            setupAdmin();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(2, result.size());
        }

        @Test
        void shouldSetSearchAndCreateTab() {
            setupAdmin();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(UserTabFactory.searchCreate().getTitle(), result.get(1).getTitle());
        }

        @Test
        void shouldSetDraftsList() {
            setupAdmin();
            setupAllResourceLinks();

            final var result = userTabsService.get();

            assertEquals(UserTabFactory.listDrafts(0).getTitle(), result.get(0).getTitle());
        }

        @Test
        void shouldSetNoTabsIfNoLinks() {
            setupAdmin();
            setupNoResourceLinks();

            final var result = userTabsService.get();

            assertEquals(0, result.size());
        }
    }

    private ResourceLinkDTO getResourceLink(ResourceLinkTypeDTO type) {
        final var link = new ResourceLinkDTO();
        link.setType(type);
        return link;
    }
}
