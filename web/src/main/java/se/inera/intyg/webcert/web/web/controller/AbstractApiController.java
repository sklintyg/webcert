package se.inera.intyg.webcert.web.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AbstractVardenhet;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.exception.FeatureNotAvailableException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractApiController.class);

    protected static final String UTF_8 = "UTF-8";

    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private WebCertUserService webCertUserService;

    protected HoSPerson createHoSPersonFromUser() {
        WebCertUser user = webCertUserService.getUser();
        return HoSPerson.create(user);
    }

    protected Vardenhet createVardenhetFromUser() {

        WebCertUser user = webCertUserService.getUser();
        AbstractVardenhet valdEnhet = getValdEnhet(user);

        Vardenhet enhet = new Vardenhet();
        enhet.setHsaId(valdEnhet.getId());
        enhet.setNamn(valdEnhet.getNamn());
        enhet.setEpost(valdEnhet.getEpost());
        enhet.setTelefonnummer(valdEnhet.getTelefonnummer());
        enhet.setPostadress(valdEnhet.getPostadress());
        enhet.setPostnummer(valdEnhet.getPostnummer());
        enhet.setPostort(valdEnhet.getPostort());
        enhet.setArbetsplatskod(valdEnhet.getArbetsplatskod());
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(user.getValdVardgivare().getId());
        vardgivare.setNamn(user.getValdVardgivare().getNamn());
        enhet.setVardgivare(vardgivare);
        return enhet;
    }

    private AbstractVardenhet getValdEnhet(WebCertUser user) {
        if (user.getValdVardenhet() instanceof AbstractVardenhet) {
            return (AbstractVardenhet) user.getValdVardenhet();
        } else {
            return null;
        }
    }

    protected List<String> getEnhetIdsForCurrentUser() {

        WebCertUser webCertUser = webCertUserService.getUser();
        List<String> vardenheterIds = webCertUser.getIdsOfSelectedVardenhet();

        LOG.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), vardenheterIds);

        return vardenheterIds;
    }

    public WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }

    protected boolean checkIfUserHasRole(UserRole... userRoles) {
        Assert.notNull(userRoles);

        List<String> list = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            list.add(userRole.name());
        }

        WebCertUser webCertUser = webCertUserService.getUser();
        return webCertUser.hasRole(list.toArray(new String[list.size()]));
    }

    protected boolean checkIfWebcertFeatureIsAvailable(WebcertFeature webcertFeature) {
        Assert.notNull(webcertFeature);
        WebCertUser webCertUser = webCertUserService.getUser();
        return webCertUser.hasAktivFunktion(webcertFeature.getName());
    }

    protected boolean checkIfWebcertFeatureIsAvailableForModule(WebcertFeature webcertFeature, String moduleType) {
        Assert.notNull(webcertFeature);
        Assert.notNull(moduleType);
        WebCertUser webCertUser = webCertUserService.getUser();
        String webcertFeatureName = StringUtils.join(new String[] { webcertFeature.getName(), moduleType }, ".");
        return webCertUser.hasAktivFunktion(webcertFeatureName);
    }

    protected void abortIfWebcertFeatureIsNotAvailable(WebcertFeature webcertFeature) {
        if (!checkIfWebcertFeatureIsAvailable(webcertFeature)) {
            throw new FeatureNotAvailableException(webcertFeature.getName());
        }
    }

    protected void abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature webcertFeature, String moduleType) {
        if (!checkIfWebcertFeatureIsAvailableForModule(webcertFeature, moduleType)) {
            throw new FeatureNotAvailableException(webcertFeature.getName());
        }
    }
}
