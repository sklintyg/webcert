package se.inera.webcert.service.intyg.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.ApplicationOrigin;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.service.intyg.dto.IntygPdf;

@Component
public class IntygModuleFacadeImpl implements IntygModuleFacade {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleFacadeImpl.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    public IntygPdf convertFromInternalToPdfDocument(String intygType, String internalIntygJsonModel) throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            PdfResponse pdfResponse = moduleApi.pdf(new InternalModelHolder(internalIntygJsonModel), ApplicationOrigin.WEBCERT);
            return new IntygPdf(pdfResponse.getPdfData(), pdfResponse.getFilename());
        } catch (ModuleException me) {
            LOG.error("ModuleException occured when when generating PDF document from internal");
            throw new IntygModuleFacadeException("ModuleException occured when generating PDF document from internal", me);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when generating PDF document from internal", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when registering certificate", e);
        }
    }

    @Override
    public CertificateResponse getCertificate(String certificateId, String intygType) throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            return moduleApi.getCertificate(certificateId, logicalAddress);
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new IntygModuleFacadeException("Exception occured when retrieving certificate", e);
        }
    }

    @Override
    public void registerCertificate(String intygType, String internalIntygJsonModel) throws ModuleException, IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            moduleApi.registerCertificate(new InternalModelHolder(internalIntygJsonModel), logicalAddress);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when registering certificate", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when registering certificate", e);
        }
    }
}
