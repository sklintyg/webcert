package se.inera.intyg.webcert.web.integration.validator;

import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;

public interface CreateDraftCertificateValidator {

    ResultValidator validate(Utlatande utlatande);
}
