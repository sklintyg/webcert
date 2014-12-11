package se.inera.webcert.service.intyg.converter;

import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.service.intyg.dto.IntygPdf;

public interface IntygModuleFacade {

    public abstract IntygPdf convertFromInternalToPdfDocument(String intygType, String internalIntygJsonModel) throws IntygModuleFacadeException;
    
    public abstract CertificateResponse getCertificate(String certificateId, String intygType) throws IntygModuleFacadeException;
 
    public abstract void registerCertificate(String intygType, String internalIntygJsonModel) throws ModuleException, IntygModuleFacadeException;
    
    public abstract void sendCertificate(String intygType, String internalIntygJsonModel, String recipient) throws ModuleException, IntygModuleFacadeException;
    
}
