package se.inera.webcert.web.controller.moduleapi;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.web.controller.moduleapi.dto.ModuleRestExceptionResponse;

public class ModuleRestExceptionHandler implements ExceptionMapper<RuntimeException> {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleRestExceptionHandler.class);
    
    @Override
    public Response toResponse(RuntimeException e) {
        ModuleRestExceptionResponse moduleResponse;
        //If this is an exception thown by our code, we have a more specific error code
        if (e instanceof WebCertServiceException) {
            //Exceptions thrown by us should be logged as warnings as they are thrown deliberately.
            LOG.warn("WebCertServiceException",e);
            moduleResponse = new ModuleRestExceptionResponse(((WebCertServiceException) e).getErrorCode(), e.getMessage());
        } else {
            //All other exceptions are logged as errors as they are not thrown deliberately.
            LOG.error("Unhandled RuntimeException",e);
            moduleResponse = new ModuleRestExceptionResponse(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());    
        }
        
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(moduleResponse).build();
    }

}
