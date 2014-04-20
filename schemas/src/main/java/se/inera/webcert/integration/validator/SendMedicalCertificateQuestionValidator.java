package se.inera.webcert.integration.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;

public class SendMedicalCertificateQuestionValidator {
    private final QuestionToFkType questionType;
    private final List<String> validationErrors = new ArrayList<>();

    public SendMedicalCertificateQuestionValidator(QuestionToFkType questionType) {
        this.questionType = questionType;
    }

    public void validateAndCorrect() {
        // First, validate properties at Revoke request level
        if (StringUtils.isEmpty(questionType.getVardReferensId())) {
            validationErrors.add("No vardReferens found!");
        }
        if (questionType.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(questionType.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(questionType.getAdressVard(), validationErrors).validateAndCorrect();

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }
}
