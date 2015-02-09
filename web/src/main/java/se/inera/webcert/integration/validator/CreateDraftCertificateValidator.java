package se.inera.webcert.integration.validator;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;

public interface CreateDraftCertificateValidator {

    ValidationResult validate(Utlatande utlatande);
}
