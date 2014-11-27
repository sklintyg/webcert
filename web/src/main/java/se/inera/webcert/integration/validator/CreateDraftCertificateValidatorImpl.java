package se.inera.webcert.integration.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.PatientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeTyp;
import se.inera.certificate.modules.registry.IntygModuleRegistry;

@Component
public class CreateDraftCertificateValidatorImpl implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;
        
    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.integration.validator.CreateDraftCertificateValidator#validate(se.inera.certificate.clinicalprocess
     * .healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType)
     */
    @Override
    public ValidationResult validate(UtlatandeType utlatande) {

        ValidationResult errors = ValidationResult.newInstance();

        validateTypAvUtlatande(utlatande.getTypAvUtlatande(), errors);
        validatePatient(utlatande.getPatient(), errors);
        validateSkapadAv(utlatande.getSkapadAv(), errors);

        return errors;
    }

    public void validateTypAvUtlatande(UtlatandeTyp typAvUtlatande, ValidationResult errors) {
    
        String intygsTyp = typAvUtlatande.getCode();
    
        if (!moduleRegistry.moduleExists(intygsTyp)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void validatePatient(PatientType patient, ValidationResult errors) {

        if (StringUtils.isBlank(patient.getEfternamn())) {
            errors.addError("efternamn is required");
        }

        if (patient.getFornamn() == null || patient.getFornamn().isEmpty()) {
            errors.addError("At least one fornamn is required");
        }
    }

    private void validateSkapadAv(HosPersonalType skapadAv, ValidationResult errors) {

        if (StringUtils.isBlank(skapadAv.getFullstandigtNamn())) {
            errors.addError("Physicians full name is required");
        }

        validateEnhet(skapadAv.getEnhet(), errors);
    }

    private void validateEnhet(EnhetType enhet, ValidationResult errors) {

        if (enhet == null) {
            errors.addError("Enhet is missing");
        }

        if (StringUtils.isBlank(enhet.getEnhetsnamn())) {
            errors.addError("enhetsnamn is required");
        }

    }
}
