package se.inera.intyg.webcert.web.service.user;

import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.List;
import java.util.Set;

public interface WebCertUserService {

    /**
     * Implementation should return the {@link WebCertUser} instance representing the currently logged in user.
     *
     * @return WebCertUser
     */
    WebCertUser getUser();

    void enableFeaturesOnUser(WebcertFeature... featuresToEnable);

    void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable);

    Set<String> getIntygstyper(String privilegeName);

    boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnits(List<String> enhetsHsaIds);

    void updateOrigin(String origin);

    void updateUserRole(String roleName);

}
