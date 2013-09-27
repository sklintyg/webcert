package se.inera.webcert.web.controller.api;


import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.security.WebCertUser;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.web.service.WebCertUserService;



public class FragaSvarApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarApiController.class);

    /**
     * Helper service to get current user.
     */
    @Autowired
    private FragaSvarService fragaSvarService;
    
    @Autowired
    private WebCertUserService webCertUserService;
    
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        LOG.debug("api.test");
        return "test";
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> list() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return fragaSvarService.getFragaSvar(user.getVardEnheter());
   
    }

}
