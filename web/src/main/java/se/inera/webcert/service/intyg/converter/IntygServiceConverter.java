package se.inera.webcert.service.intyg.converter;

import java.util.List;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.model.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;

public interface IntygServiceConverter {

    public abstract SendType buildSendTypeFromUtlatande(Utlatande utlatande);
    
    public abstract List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source);
    
    public abstract IntygMetadata convertToIntygMetadata(String patientId, CertificateMetaType source);

    public abstract RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage);
    
    public abstract String extractUtlatandeId(Utlatande utlatande);
}
