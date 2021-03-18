package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.common.support.modules.support.facade.dto.UserDTO;

public interface UserService {

    UserDTO getLoggedInUser();
}
