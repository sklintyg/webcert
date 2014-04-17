package se.inera.webcert.web.handlers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

/**
 * Exception handler for REST services. Runtime exceptions thrown as
 * {@link WebCertServiceException} WebCertServiceException are logged as
 * warnings. All other runtime exceptions thrown are treated as errors and
 * logged as such.
 *
 * @author nikpet
 */
public class WebcertRestExceptionHandler implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRestExceptionHandler.class);

    @Override
    public Response toResponse(RuntimeException e) {
        WebcertRestExceptionResponse exceptionResponse;
        // If this is an exception thrown by our code, we have a more specific error code
        if (e instanceof WebCertServiceException) {
            WebCertServiceException ex = (WebCertServiceException) e;
            // Exceptions thrown by us should be logged as warnings as they are
            // thrown deliberately.
            LOG.warn("Internal exception occured! Internal error code: {} Error message: {}", ex.getErrorCode(),
                    e.getMessage());
            exceptionResponse = new WebcertRestExceptionResponse(ex.getErrorCode(), e.getMessage());
        } else {
            // All other exceptions are logged as errors as they are not thrown
            // deliberately.
            LOG.error("Unhandled RuntimeException occured!", e);
            exceptionResponse = new WebcertRestExceptionResponse(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                    e.getMessage());
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
                .build();
    }

}
