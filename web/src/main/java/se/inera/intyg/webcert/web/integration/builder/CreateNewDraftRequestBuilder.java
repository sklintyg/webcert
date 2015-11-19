package se.inera.webcert.integration.builder;

import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;

public interface CreateNewDraftRequestBuilder {

    CreateNewDraftRequest buildCreateNewDraftRequest(Utlatande utlatandeRequest, MiuInformationType unitMIU);
}
