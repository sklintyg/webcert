package se.inera.intyg.webcert.web.service.intyg.converter;

import se.inera.certificate.model.Status;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

import java.util.List;

public interface IntygModuleFacade {

    IntygPdf convertFromInternalToPdfDocument(String intygType, String internalIntygJsonModel, List<Status> statuses, boolean isEmployer) throws IntygModuleFacadeException;

    CertificateResponse getCertificate(String certificateId, String intygType) throws IntygModuleFacadeException;

    void registerCertificate(String intygType, String internalIntygJsonModel) throws ModuleException, IntygModuleFacadeException;

}
