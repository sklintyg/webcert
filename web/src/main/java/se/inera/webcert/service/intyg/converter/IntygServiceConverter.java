package se.inera.webcert.service.intyg.converter;

import java.util.List;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
import se.inera.webcert.service.intyg.dto.IntygStatus;

public interface IntygServiceConverter {

    public abstract List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source);
    
    public abstract IntygMetadata convertToIntygMetadata(Utlatande utlatande, CertificateMetaData meta);
    
    public abstract List<IntygStatus> convertListOfStatusToListOfIntygStatus(List<Status> source);
    
    public abstract IntygMetadata convertToIntygMetadata(String patientId, CertificateMetaType source);

    public abstract SendType buildSendTypeFromUtlatande(Utlatande utlatande);
    
    public abstract RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage);
}
