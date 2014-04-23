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
        // First, validate properties at SendMedicalCertificateQuestion request level
        if (StringUtils.isEmpty(questionType.getVardReferensId())) {
            validationErrors.add("No vardReferens-id found!");
        }
        if (questionType.getAmne() == null) {
            validationErrors.add("No Amne element found!");
        }
        if (questionType.getFraga() == null) {
            validationErrors.add("No Question fraga element found!");
        } else {
            if (StringUtils.isEmpty(questionType.getFraga().getMeddelandeText())) {
                validationErrors.add("No Question fraga meddelandeText elements found or set!");
            }
            if (questionType.getFraga().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Question fraga signeringsTidpunkt elements found or set!");
            }
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
