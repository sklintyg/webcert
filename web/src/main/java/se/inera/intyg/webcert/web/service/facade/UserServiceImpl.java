package se.inera.intyg.webcert.web.service.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.support.facade.dto.UserDTO;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
public class UserServiceImpl implements UserService {

    private final WebCertUserService webCertUserService;

    @Autowired
    public UserServiceImpl(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    @Override
    public UserDTO getLoggedInUser() {
        final var webCertUser = webCertUserService.getUser();
        final var loggedInUser = new UserDTO();
        loggedInUser.setHsaId(webCertUser.getHsaId());
        loggedInUser.setName(webCertUser.getNamn());
        loggedInUser.setRole(
            webCertUser.getRoleTypeName().equalsIgnoreCase("VARDADMINISTRATOR") ? "Vårdadministratör" : webCertUser.getRoleTypeName());
        loggedInUser.setLoggedInUnit(webCertUser.getValdVardenhet().getNamn());
        loggedInUser.setLoggedInCareProvider(webCertUser.getValdVardgivare().getNamn());
        return loggedInUser;
    }
}
