package se.inera.intyg.webcert.web.service.facade.list.config;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.StaffListInfo;

import java.util.List;

public interface GetStaffInfoFacadeService {
    List<StaffListInfo> get();

    String getLoggedInStaffHsaId();
}
