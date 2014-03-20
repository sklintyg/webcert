package se.inera.certificate.mc2wc.exception;

import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

/**
 * Maps HTTP status codes in the response to different types of exceptions.
 *
 * @author nikpet
 */
public class CertificateMigrationResponseExceptionMapper implements
        ResponseExceptionMapper<AbstractCertificateMigrationException> {

    private static Logger log = LoggerFactory.getLogger(CertificateMigrationResponseExceptionMapper.class);

    public CertificateMigrationResponseExceptionMapper() {

    }

    @Override
    public AbstractCertificateMigrationException fromResponse(Response response) {

        StatusType statusInfo = response.getStatusInfo();
        Family responseFamily = statusInfo.getFamily();

        if (responseFamily.equals(Family.CLIENT_ERROR)) {
            log.error("A client error (HTTP code {}) occured when migrating Certificate", statusInfo.getStatusCode());

            return new CertificateMigrationException();
        } else if (responseFamily.equals(Family.SERVER_ERROR)) {
            log.error("A server error (HTTP code {}) occured when migrating Certificate {}", statusInfo.getStatusCode());

            return new FatalCertificateMigrationException();
        }

        return null;
    }

}
