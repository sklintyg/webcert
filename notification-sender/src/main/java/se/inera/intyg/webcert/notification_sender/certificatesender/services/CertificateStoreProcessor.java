package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;

/**
 * Camel message processor responsible for consuming {@link Constants#STORE_MESSAGE} messages,
 * using the ModuleApi to register certificates in intygstjansten.
 */
public class CertificateStoreProcessor {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public void process(@Body String utkastAsJson,
            @Header(Constants.INTYGS_TYP) String intygsTyp,
            @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws Exception {

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);

        try {
            moduleApi.registerCertificate(new InternalModelHolder(utkastAsJson), logicalAddress);
        } catch (ExternalServiceCallException e) {
            switch (e.getErroIdEnum()) {
            case TECHNICAL_ERROR:
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
