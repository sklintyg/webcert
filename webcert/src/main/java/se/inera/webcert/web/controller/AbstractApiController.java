package se.inera.webcert.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.AbstractVardenhet;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.web.service.WebCertUserService;

public abstract class AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractApiController.class);

    protected static final String UTF_8 = "UTF-8";

    protected static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private WebCertUserService webCertUserService;

    protected HoSPerson createHoSPersonFromUser() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return HoSPerson.create(user);
    }

    protected Vardenhet createVardenhetFromUser() {
        
        WebCertUser user = webCertUserService.getWebCertUser();
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
        if (user.getValdVardenhet() instanceof  AbstractVardenhet) {
            return (AbstractVardenhet) user.getValdVardenhet();
        } else {
            return null;
        }
    }
    protected List<String> getEnhetIdsForCurrentUser() {

        WebCertUser webCertUser = webCertUserService.getWebCertUser();
        List<String> vardenheterIds = webCertUser.getIdsOfSelectedVardenhet();

        LOG.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), vardenheterIds);

        return vardenheterIds;
    }

    public WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }
}
