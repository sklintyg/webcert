package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.web.controller.moduleapi.dto.ModuleRestExceptionResponse;

public class ModuleRestExceptionHandler implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException e) {
        ModuleRestExceptionResponse moduleResponse;
        //If this is an exception thown by our code, we have a more specific error code
        if (e instanceof WebCertServiceException) {
            moduleResponse = new ModuleRestExceptionResponse(((WebCertServiceException) e).getErrorCode(), e.getMessage());
        } else {
            moduleResponse = new ModuleRestExceptionResponse(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());    
        }
        
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(moduleResponse).build();
    }

}
