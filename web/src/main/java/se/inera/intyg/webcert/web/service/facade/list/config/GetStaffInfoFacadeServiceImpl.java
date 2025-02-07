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
package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesInfoAggregator;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStaffInfoFacadeServiceImpl implements GetStaffInfoFacadeService {

    private final WebCertUserService webCertUserService;
    private final UtkastService utkastService;
    private final ArendeService arendeService;
    private final ListCertificatesInfoAggregator listCertificatesInfoAggregator;

    @Override
    public List<StaffListInfo> get() {
        return getStaffInfo();
    }

    @Override
    public List<StaffListInfo> get(String unitId) {
        return getStaffInfo(unitId);
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
    public List<String> getIdsOfSelectedUnit() {
        final var webCertUser = webCertUserService.getUser();
        final var units = webCertUser.getIdsOfSelectedVardenhet();
        log.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), units);
        return units;
    }

    private List<StaffListInfo> getStaffInfo() {
        final var staffListFromWC = listStaffFromWC();
        final var staffListFromCS = listCertificatesInfoAggregator.listCertificatesInfoForUnit();
        return Stream.concat(
                staffListFromWC.stream(),
                staffListFromCS.stream().filter(ifNotAlreadyInStaffListFromWC(staffListFromWC))
            )
            .collect(Collectors.toList());
    }

    private static Predicate<StaffListInfo> ifNotAlreadyInStaffListFromWC(List<StaffListInfo> staffListFromWC) {
        return staffListInfo ->
            staffListFromWC.stream().noneMatch(staffWc ->
                staffWc.getHsaId().equals(staffListInfo.getHsaId())
            );
    }

    private List<StaffListInfo> listStaffFromWC() {
        final var user = webCertUserService.getUser();
        final var selectedUnitHsaId = user.getValdVardenhet().getId();
        final var staff = utkastService.getLakareWithDraftsByEnhet(selectedUnitHsaId);
        addUserToList(staff, user);
        return convertStaffList(staff);
    }

    private void addUserToList(List<Lakare> staff, WebCertUser user) {
        if (isUserMissingFromList(staff, user) && user.isLakare()) {
            staff.add(new Lakare(user.getHsaId(), user.getNamn()));
        }
    }

    private List<StaffListInfo> getStaffInfo(String unitId) {
        final var user = webCertUserService.getUser();
        final var staff = arendeService.listSignedByForUnits(unitId.length() > 0 ? unitId : null);
        addUserToList(staff, user);
        return convertStaffList(staff);
    }

    private boolean isUserMissingFromList(List<Lakare> staff, WebCertUser user) {
        return staff.stream().noneMatch((doctor) -> doctor.getHsaId().equals(user.getHsaId()));
    }

    private List<StaffListInfo> convertStaffList(List<Lakare> staff) {
        return staff.stream().map(this::convertStaff).collect(Collectors.toList());
    }

    private StaffListInfo convertStaff(Lakare lakare) {
        return new StaffListInfo(lakare.getHsaId(), lakare.getName());
    }


}
