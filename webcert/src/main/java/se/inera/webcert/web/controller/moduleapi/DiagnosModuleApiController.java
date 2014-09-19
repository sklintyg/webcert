package se.inera.webcert.web.controller.moduleapi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.service.diagnos.DiagnosService;
import se.inera.webcert.service.diagnos.model.Diagnos;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.DiagnosResponse;

public class DiagnosModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosModuleApiController.class);

    @Autowired
    private DiagnosService diagnosService;

    @POST
    @Path("/kod")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getDiagnosisByCode(String code) {

        LOG.debug("Getting diagnosis using code: {}", code);

        DiagnosResponse response = new DiagnosResponse();

        Diagnos diagnos = diagnosService.getDiagnosisByCode(code);

        if (diagnos != null) {
            response.setDiagnos(diagnos);
        } else {
            LOG.debug("Diagnosis was not found using code: {}", code);
            response.setResultatNotFound();
        }

        return Response.ok(response).build();
    }
    
    @POST
    @Path("/kod/sok")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response searchDiagnosisByCode(String codeFragment) {

        LOG.debug("Searching for diagnosises using code fragment: {}", codeFragment);

        DiagnosResponse response = new DiagnosResponse();

        List<Diagnos> results = diagnosService.searchDiagnosisByCode(codeFragment);

        if (results.isEmpty()) {
            LOG.debug("Diagnosis was not found using code: {}", codeFragment);
            response.setResultatNotFound();
        } else {
            response.setDiagnoser(results);
        }

        return Response.ok(response).build();
    }
}
