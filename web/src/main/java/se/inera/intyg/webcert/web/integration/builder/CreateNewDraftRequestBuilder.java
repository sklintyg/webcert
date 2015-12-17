package se.inera.intyg.webcert.web.integration.builder;

import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.infrastructure.directory.v1.CommissionType;

public interface CreateNewDraftRequestBuilder {

    CreateNewDraftRequest buildCreateNewDraftRequest(Utlatande utlatandeRequest, CommissionType unitMIU);
}
