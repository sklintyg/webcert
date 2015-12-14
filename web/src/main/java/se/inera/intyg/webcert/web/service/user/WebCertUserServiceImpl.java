package se.inera.intyg.webcert.web.service.user;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.RequestOrigin;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.security.WebCertUserOrigin;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class WebCertUserServiceImpl implements WebCertUserService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserService.class);

    @Autowired
    private AuthoritiesResolver authoritiesResolver;

    @Override
    public WebCertUser getUser() {
        return (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

//    @Override
//    public void assertUserRoles(String[] grantedRoles) throws AuthoritiesException {
//        Map<String, Role> roles = getUser().getRoles();
//
//        List<String> gr = Arrays.asList(grantedRoles);
//        for (String role : roles.keySet()) {
//            if (gr.contains(role)) {
//                return;
//            }
//        }
//
//        throw new AuthoritiesException(
//                String.format("User does not have a valid role for current task. User's role must be one of [%s] but was [%s]",
//                        StringUtils.join(grantedRoles, ","), StringUtils.join(roles.keySet(), ",")));
//
//    }
//
//    @Override
//    public void assertRequestOrigin(String requestOrigin) throws AuthoritiesException {
//        String origin = getUser().getOrigin();
//        if (origin.equals(requestOrigin)) {
//            return;
//        }
//
//        throw new AuthoritiesException(
//                String.format("User does not have the valid request origin for current task. User's origin must be [%s] but was [%s]",
//                        origin, requestOrigin));
//    }
//
//    @Override
//    public void clearEnabledFeaturesOnUser() {
//        WebCertUser user = getUser();
//        user.getAktivaFunktioner().clear();
//
//        LOG.debug("Cleared enabled featured on user {}", user.getHsaId());
//    }

    @Override
    public void enableFeaturesOnUser(WebcertFeature... featuresToEnable) {
        enableFeatures(getUser(), featuresToEnable);
    }

    @Override
    public void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable) {
        Assert.notNull(moduleName);
        Assert.notEmpty(modulefeaturesToEnable);

        enableModuleFeatures(getUser(), moduleName, modulefeaturesToEnable);
    }

    @Override
    public boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        return checkIfAuthorizedForUnit(getUser(), vardgivarHsaId, enhetsHsaId, isReadOnlyOperation);
    }

    @Override
    public boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation) {
        return checkIfAuthorizedForUnit(getUser(), null, enhetsHsaId, isReadOnlyOperation);
    }

    @Override
    public boolean isAuthorizedForUnits(List<String> enhetsHsaIds) {
        WebCertUser user = getUser();
        return user != null && user.getIdsOfSelectedVardenhet().containsAll(enhetsHsaIds);
    }

    @Override
    public void updateOrigin(String origin) {
        getUser().setOrigin(origin);
    }

    @Override
    public void updateUserRole(String roleName) {
        updateUserRole(authoritiesResolver.getRole(roleName));
    }

    public void updateUserRole(Role role) {
        getUser().setRoles(AuthoritiesResolverUtil.toMap(role));
        getUser().setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
    }

    // - - - - - Package scope - - - - -

    boolean checkIfAuthorizedForUnit(WebCertUser user, String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        if (user == null) {
            return false;
        }

        String origin = user.getOrigin();
        if (origin.equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
            if (isReadOnlyOperation && vardgivarHsaId != null) {
                return user.getValdVardgivare().getId().equals(vardgivarHsaId);
            }
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        } else {
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        }
    }

    void enableFeatures(WebCertUser user, WebcertFeature... featuresToEnable) {
        LOG.debug("User {} had these features: {}", user.getHsaId(), StringUtils.join(user.getAktivaFunktioner(), ", "));

        for (WebcertFeature feature : featuresToEnable) {
            user.getAktivaFunktioner().add(feature.getName());
        }

        LOG.debug("User {} now has these features: {}", user.getHsaId(), StringUtils.join(user.getAktivaFunktioner(), ", "));
    }

    void enableModuleFeatures(WebCertUser user, String moduleName, ModuleFeature... modulefeaturesToEnable) {
        for (ModuleFeature moduleFeature : modulefeaturesToEnable) {

            String moduleFeatureName = moduleFeature.getName();
            String moduleFeatureStr = StringUtils.join(new String[] { moduleFeatureName, moduleName.toLowerCase() }, ".");

            if (!user.hasAktivFunktion(moduleFeatureName)) {
                LOG.warn("Could not add module feature '{}' to user {} since corresponding webcert feature is not enabled", moduleFeatureStr,
                        user.getHsaId());
                continue;
            }

            user.getAktivaFunktioner().add(moduleFeatureStr);
            LOG.debug("Added module feature {} to user", moduleFeatureStr);
        }
    }

}
