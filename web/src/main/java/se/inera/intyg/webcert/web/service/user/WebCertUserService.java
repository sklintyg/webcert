package se.inera.intyg.webcert.web.service.user;

import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;

import java.util.List;

public interface WebCertUserService {

    /**
     * Implementation should return the {@link WebCertUser} instance representing the currently logged in user.
     *
     * @return WebCertUser
     */
    WebCertUser getUser();

    void assertUserRoles(String[] grantedRoles) throws AuthoritiesException;

    void clearEnabledFeaturesOnUser();

    void enableFeaturesOnUser(WebcertFeature... featuresToEnable);

    void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable);

    boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnits(List<String> enhetsHsaIds);

    void updateUserRole(String roleName);

}
