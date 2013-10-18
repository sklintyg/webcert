package se.inera.webcert.web.controller.api;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.web.service.WebCertUserService;



public class FragaSvarApiController {

    /**
     * Helper service to get current user.
     */
    @Autowired
    private FragaSvarService fragaSvarService;
    
    @Autowired
    private WebCertUserService webCertUserService;
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> list() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return fragaSvarService.getFragaSvar(user.getVardenheterIds());
    }
}
