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

package se.inera.intyg.webcert.web.service.facade.list.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetStaffInfoFacadeServiceImpl implements GetStaffInfoFacadeService {

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;
    private static final Logger LOG = LoggerFactory.getLogger(GetStaffInfoFacadeServiceImpl.class);

    @Autowired
    public GetStaffInfoFacadeServiceImpl(WebCertUserService webCertUserService, UtkastService utkastService) {
        this.webCertUserService = webCertUserService;
        this.utkastService = utkastService;
    }

    @Override
    public List<StaffListInfo> get() {
        return getStaffInfo();
    }

    @Override
    public String getLoggedInStaffHsaId() {
        return webCertUserService.getUser().getHsaId();
    }

    @Override
    public boolean isLoggedInUserDoctor() {
        return webCertUserService.getUser().isLakare() || webCertUserService.getUser().isPrivatLakare();
    }

    @Override
    public List<String> getUnits() {
        WebCertUser webCertUser = webCertUserService.getUser();
        final var units = webCertUser.getIdsOfSelectedVardenhet();
        LOG.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), units);
        return units;
    }

    private List<StaffListInfo> getStaffInfo() {
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();

        final var staff = utkastService.getLakareWithDraftsByEnhet(selectedUnitHsaId);
        return convertStaffList(staff);
    }

    private List<StaffListInfo> convertStaffList(List<Lakare> staff) {
        return staff.stream().map(this::convertStaff).collect(Collectors.toList());
    }

    private StaffListInfo convertStaff(Lakare lakare) {
        return new StaffListInfo(lakare.getHsaId(), lakare.getName());
    }


}
