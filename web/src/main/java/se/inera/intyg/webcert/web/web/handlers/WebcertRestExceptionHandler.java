package se.inera.intyg.webcert.web.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.web.service.exception.FeatureNotAvailableException;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception handler for REST services. Runtime exceptions thrown as {@link WebCertServiceException}
 * WebCertServiceException are logged as warnings. All other runtime exceptions thrown are treated as errors and
 * logged as such.
 *
 * @author nikpet
 */
public class WebcertRestExceptionHandler implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRestExceptionHandler.class);

    @Override
    public Response toResponse(RuntimeException e) {

        if (e instanceof WebCertServiceException) {
            // If this is an exception thrown by our code, we have a more specific error code
            return handleWebCertServiceException((WebCertServiceException) e);
        } else if (e instanceof FeatureNotAvailableException) {
            return handleFeatureNotAvailableException((FeatureNotAvailableException) e);
        }

        return handleRuntimeException(e);

    }

    private Response handleFeatureNotAvailableException(FeatureNotAvailableException e) {
        String msg = "Not available since feature is not active";
        return Response.status(Status.FORBIDDEN).entity(msg).build();
    }

    /**
     * Exceptions thrown by us should be logged as warnings as they are thrown deliberately.
     *
     * @param wcse
     *            A WebCertServiceException
     * @return
     */
    private Response handleWebCertServiceException(WebCertServiceException wcse) {
        // Don't log concurrent modifiation exceptions, they are logged elsewhere
        if (wcse.getErrorCode() != WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION) {
            LOG.warn("Internal exception occured! Internal error code: {} Error message: {}", wcse.getErrorCode(),
                    wcse.getMessage());
        }
        WebcertRestExceptionResponse exceptionResponse = new WebcertRestExceptionResponse(wcse.getErrorCode(), wcse.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * All other runtime exceptions are logged as errors as they are not thrown deliberately.
     *
     * @param re
     *            A RuntimeException
     * @return
     */
    private Response handleRuntimeException(RuntimeException re) {
        LOG.error("Unhandled RuntimeException occured!", re);
        WebcertRestExceptionResponse exceptionResponse = new WebcertRestExceptionResponse(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                re.getMessage());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exceptionResponse).type(MediaType.APPLICATION_JSON)
                .build();
    }
}
