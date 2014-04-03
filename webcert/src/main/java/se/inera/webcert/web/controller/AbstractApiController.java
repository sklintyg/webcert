package se.inera.webcert.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.web.service.WebCertUserService;

public abstract class AbstractApiController {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractApiController.class);

    protected static final String UTF_8 = "UTF-8";

    protected static final String UTF_8_CHARSET = ";charset=utf-8";
    
    @Autowired
    protected WebCertUserService webCertUserService;
    
    protected HoSPerson createHoSPersonFromUser() {

        WebCertUser user = webCertUserService.getWebCertUser();

        HoSPerson hosp = new HoSPerson();
        hosp.setNamn(user.getNamn());
        hosp.setHsaId(user.getHsaId());
        hosp.setForskrivarkod(user.getForskrivarkod());

        // TODO The users befattning needs to be supplied

        return hosp;
    }
    
    protected List<String> getEnhetIdsForCurrentUser() {

        WebCertUser webCertUser = webCertUserService.getWebCertUser();
        List<String> vardenheterIds = webCertUser.getIdsOfSelectedVardenhet();
        
        LOG.debug("Current user '{}' has assignments: {}", webCertUser.getHsaId(), vardenheterIds);
        
        return vardenheterIds;
    }
}
