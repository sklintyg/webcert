package se.inera.webcert.service.intyg.converter;

import java.util.List;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.model.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;

public interface IntygServiceConverter {

    SendType buildSendTypeFromUtlatande(Utlatande utlatande);

    List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source);

    IntygMetadata convertToIntygMetadata(String patientId, CertificateMetaType source);

    RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage);

    String extractUtlatandeId(Utlatande utlatande);
}
