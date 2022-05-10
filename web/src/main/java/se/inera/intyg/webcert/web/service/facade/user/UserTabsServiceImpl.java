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
package se.inera.intyg.webcert.web.service.facade.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetUserResourceLinksImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserTabsServiceImpl implements UserTabsService {

    private final WebCertUserService webCertUserService;
    private final GetUserResourceLinksImpl getUserResourceLinks;
    private final UserStatisticsServiceImpl userStatisticsService;

    @Autowired
    public UserTabsServiceImpl(WebCertUserService webCertUserService, GetUserResourceLinksImpl getUserResourceLinks,
                               UserStatisticsServiceImpl userStatisticsService) {
        this.webCertUserService = webCertUserService;
        this.getUserResourceLinks = getUserResourceLinks;
        this.userStatisticsService = userStatisticsService;
    }

    @Override
    public List<UserTab> get() {
        final var user = webCertUserService.getUser();
        final var links = getUserResourceLinks.get(user);
        return getTabs(links, user);
    }

    private List<UserTab> getTabs(ResourceLinkDTO[] links, WebCertUser user) {
        if (user.isLakare() || user.isPrivatLakare()) {
            return getTabsForDoctor(links);
        } else {
            return getTabsForAdmin(links);
        }
    }

    private List<UserTab> getTabsForDoctor(ResourceLinkDTO[] links) {
        final var tabs = new ArrayList<UserTab>();
        final var linkList = Arrays.asList(links);
        addSearchCreate(tabs, linkList);
        addListDrafts(tabs, linkList);
        addListCertificates(tabs, linkList);
        return tabs;
    }

    private void addListDrafts(ArrayList<UserTab> tabs, List<ResourceLinkDTO> linkList) {
        if (doesLinkListContain(linkList, ResourceLinkTypeDTO.ACCESS_DRAFT_LIST)) {
            tabs.add(UserTabFactory.listDrafts(userStatisticsService.getNumberOfDraftsOnSelectedUnit()));
        }
    }

    private void addSearchCreate(ArrayList<UserTab> tabs, List<ResourceLinkDTO> linkList) {
        if (doesLinkListContain(linkList, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE)) {
            tabs.add(UserTabFactory.searchCreate());
        }
    }

    private void addListCertificates(ArrayList<UserTab> tabs, List<ResourceLinkDTO> linkList) {
        if (doesLinkListContain(linkList, ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST)) {
            tabs.add(UserTabFactory.signedCertificates());
        }
    }

    private List<UserTab> getTabsForAdmin(ResourceLinkDTO[] links) {
        final var tabs = new ArrayList<UserTab>();
        final var linkList = Arrays.asList(links);
        addListDrafts(tabs, linkList);
        addSearchCreate(tabs, linkList);
        return tabs;
    }

    private boolean doesLinkListContain(List<ResourceLinkDTO> linkList, ResourceLinkTypeDTO type) {
        return linkList.stream().anyMatch((ResourceLinkDTO link) -> link.getType() == type);
    }

}
