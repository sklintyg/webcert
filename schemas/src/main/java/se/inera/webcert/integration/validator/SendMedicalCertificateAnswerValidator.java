package se.inera.webcert.integration.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;

public class SendMedicalCertificateAnswerValidator {
    private final AnswerToFkType answerType;
    private final List<String> validationErrors = new ArrayList<>();

    public SendMedicalCertificateAnswerValidator(AnswerToFkType answerType) {
        this.answerType = answerType;
    }

    public void validateAndCorrect() {
        // First, validate properties at Revoke request level
        if (StringUtils.isEmpty(answerType.getVardReferensId())) {
            validationErrors.add("No vardReferens found!");
        }
        if (answerType.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(answerType.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(answerType.getAdressVard(), validationErrors).validateAndCorrect();
 
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }
}
