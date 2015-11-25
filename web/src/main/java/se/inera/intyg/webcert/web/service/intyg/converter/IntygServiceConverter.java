package se.inera.intyg.webcert.web.service.intyg.converter;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItem;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

import java.util.List;


public interface IntygServiceConverter {

    List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source);

    List<IntygItem> convertDraftsToListOfIntygItem(List<Utkast> drafts);

    SendType buildSendTypeFromUtlatande(Utlatande utlatande);

    RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage);

    List<se.inera.intyg.common.support.model.Status> buildStatusesFromUtkast(Utkast draft);

    Utlatande buildUtlatandeFromUtkastModel(Utkast utkast);
}
