package se.inera.webcert.service.intyg.converter;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.ApplicationOrigin;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.service.intyg.dto.IntygPdf;

@Component
public class IntygModuleFacadeImpl implements IntygModuleFacade {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleFacadeImpl.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;
    
    @Autowired
    private IntygModuleModelJaxbUtil jaxbUtil;

    public IntygPdf convertFromExternalToPdfDocument(String intygType, String externalIntygJsonModel) throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            PdfResponse pdfResponse = moduleApi.pdf(new ExternalModelHolder(externalIntygJsonModel), ApplicationOrigin.WEBCERT);
            return new IntygPdf(pdfResponse.getPdfData(), pdfResponse.getFilename());
        } catch (ModuleException e) {
            throw new IntygModuleFacadeException("Exception occured when generating PDF document from external", e);
        }
    }

    public String convertFromExternalToInternal(String intygType, String externalIntygJsonModel) throws IntygModuleFacadeException {

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

        ExternalModelHolder extHolder = new ExternalModelHolder(externalIntygJsonModel);
        InternalModelResponse internalModelReponse;
        try {
            internalModelReponse = moduleApi.convertExternalToInternal(extHolder);
        } catch (ModuleException me) {
            throw new IntygModuleFacadeException("Exception occured when converting from external to internal", me);
        }

        return internalModelReponse.getInternalModel();
    }

    public ExternalModelResponse convertFromTransportToExternal(String intygType, UtlatandeType utlatandeType)
            throws IntygModuleFacadeException {
        try {
            String xml = jaxbUtil.marshallFromTransportToXml(utlatandeType);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            return moduleApi.unmarshall(new TransportModelHolder(xml));
        } catch (JAXBException jxe) {
            throw new IntygModuleFacadeException("Exception occured when marshalling from transport to XML", jxe);
        } catch (ModuleException me) {
            throw new IntygModuleFacadeException("Exception occured when unmarshalling from transport XML to external", me);
        }
    }

    @Override
    public UtlatandeType convertFromInternalToTransport(String intygType, String internalIntygJsonModel) throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

            ExternalModelResponse external = moduleApi.convertInternalToExternal(new InternalModelHolder(internalIntygJsonModel));

            ExternalModelHolder holder = new ExternalModelHolder(external.getExternalModelJson());
            TransportModelResponse modelResponse = moduleApi.marshall(holder, TransportModelVersion.UTLATANDE_V1);

            return jaxbUtil.unmarshallFromXmlToTransport(modelResponse.getTransportModel());

        } catch (ModuleException me) {
            throw new IntygModuleFacadeException("A ModuleException was throw when converting from internal to transport", me);
        } catch (JAXBException jxe) {
            throw new IntygModuleFacadeException("Exception occured when unmarshalling from XML to transport", jxe);
        }
    }

    @Override
    public Utlatande convertFromInternalToExternal(String intygType, String internalIntygJsonModel) throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

            ExternalModelResponse moduleResponse = moduleApi.convertInternalToExternal(new InternalModelHolder(internalIntygJsonModel));

            return moduleResponse.getExternalModel();

        } catch (ModuleException e) {
            throw new IntygModuleFacadeException("A ModuleException was throw when converting from internal to external", e);
        }
    }
    
}
