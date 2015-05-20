package se.inera.webcert.certificatesender.services;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.notifications.routes.RouteHeaders;

import com.google.common.annotations.VisibleForTesting;

import javax.xml.ws.WebServiceException;

@Component
public class CertificateStoreProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateStoreProcessor.class);

    private static final java.lang.String INTYGS_TYP = "INTYGS_TYP";
    private static final java.lang.String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @VisibleForTesting
    void setModuleRegistry(IntygModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public void process(Message message) throws Exception {
        LOG.debug("Receiving message: {}", message.getMessageId());

        ModuleApi moduleApi = moduleRegistry.getModuleApi((String) message.getHeader(INTYGS_TYP));

        try {
            moduleApi.registerCertificate(new InternalModelHolder((String) message.getBody()), (String) message.getHeader(LOGICAL_ADDRESS));
        } catch (ExternalServiceCallException e) {
            switch (e.getErroIdEnum()) {
                case TECHNICAL_ERROR:
                    // TODO: is this temporary?
                case APPLICATION_ERROR:
                    throw new TemporaryException(e.getMessage());
                default:
                    throw new PermanentException(e.getMessage());
            }
        } catch (ModuleException e) {
            throw new PermanentException(e.getMessage());
        } catch (WebServiceException e) {
            throw new TemporaryException(e.getMessage());
        } catch (Exception e) {
            throw new PermanentException(e.getMessage());
        }
    }

}
