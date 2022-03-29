package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public interface GetStaffInfoFacadeService {
    List<StaffListInfoDTO> get();

    String getLoggedInStaffHsaId();
}
