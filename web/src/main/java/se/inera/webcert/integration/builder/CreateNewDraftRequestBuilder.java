package se.inera.webcert.integration.builder;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;

public interface CreateNewDraftRequestBuilder {

    CreateNewDraftRequest buildCreateNewDraftRequest(UtlatandeType utlatandeRequest, MiuInformationType unitMIU);
}
