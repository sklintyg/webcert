package se.inera.webcert.integration.validator;

import iso.v21090.dt.v1.II;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.PatientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeTyp;
import se.inera.certificate.model.Id;
import se.inera.certificate.validate.IdValidator;
import se.inera.certificate.validate.SimpleIdValidatorBuilder;
import se.inera.webcert.modules.IntygModuleRegistry;

@Component
public class CreateDraftCertificateValidatorImpl implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;
        
    private IdValidator idValidator;
    
    @PostConstruct
    public void init() {
        SimpleIdValidatorBuilder validatorBuilder = new SimpleIdValidatorBuilder();
        this.idValidator = validatorBuilder.withSimpleHsaIdValidator().withPersonnummerValidator(true).withSamordningsnummerValidator(true).build();
    }

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

        validateId(patient.getPersonId(), "person-id", errors);

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

        validateId(skapadAv.getPersonalId(), "personal-id", errors);

        validateEnhet(skapadAv.getEnhet(), errors);
    }

    private void validateEnhet(EnhetType enhet, ValidationResult errors) {

        if (enhet == null) {
            errors.addError("Enhet is missing");
        }

        if (StringUtils.isBlank(enhet.getEnhetsnamn())) {
            errors.addError("enhetsnamn is required");
        }

        validateId(enhet.getEnhetsId(), "enhets-id", errors);
    }

    public void validateId(II iI, String name, ValidationResult errors) {
        
        Id id = new Id(iI.getRoot(), iI.getExtension());
        
        if (StringUtils.isBlank(iI.getRoot())) {
            errors.addError("Element {0} is missing root element", name);
            return;
        }
        
        if (!idValidator.isValidationSupported(id)) {
            errors.addError("Validation is not supported for root {1} in element {0}", name, id.getRoot());
            return;
        }
        
        List<String> results = idValidator.validate(id);
        
        if (!results.isEmpty()) {
            String resultsStr = StringUtils.join(results, ", ");
            errors.addError("Id element {0} has errors: [{1}]", name, resultsStr);
        }
        
    }
}
