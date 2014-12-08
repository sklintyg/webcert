package se.inera.webcert.service.intyg.converter;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.webcert.service.intyg.dto.IntygPdf;

public interface IntygModuleFacade {

    UtlatandeType convertFromInternalToTransport(String intygType, String intygJsonModel) throws IntygModuleFacadeException;

    Utlatande convertFromInternalToExternal(String intygType, String intygJsonModel) throws IntygModuleFacadeException;

    ExternalModelResponse convertFromTransportToExternal(String intygType, UtlatandeType utlatandeType) throws IntygModuleFacadeException;

    String convertFromExternalToInternal(String intygType, String externalIntygJsonModel) throws IntygModuleFacadeException;

    IntygPdf convertFromExternalToPdfDocument(String intygType, String externalIntygJsonModel) throws IntygModuleFacadeException;
}
