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
        // First, validate properties at SendMedicalCertificateAnswer request level
        if (StringUtils.isEmpty(answerType.getVardReferensId())) {
            validationErrors.add("No vardReferens-id found!");
        }
        if (StringUtils.isEmpty(answerType.getFkReferensId())) {
            validationErrors.add("No fkReferens-id found!");
        }
        if (answerType.getAmne() == null) {
            validationErrors.add("No Amne element found!");
        }
        if (answerType.getFraga() == null) {
            validationErrors.add("No Answer fraga element found!");
        } else {
            if (StringUtils.isEmpty(answerType.getFraga().getMeddelandeText())) {
                validationErrors.add("No Answer fraga meddelandeText elements found or set!");
            }
            if (answerType.getFraga().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Answer fraga signeringsTidpunkt elements found or set!");
            }
        }
        if (answerType.getSvar() == null) {
            validationErrors.add("No Answer svar element found!");
        } else {
            if (StringUtils.isEmpty(answerType.getSvar().getMeddelandeText())) {
                validationErrors.add("No Answer svar meddelandeText elements found or set!");
            }
            if (answerType.getSvar().getSigneringsTidpunkt() == null) {
                validationErrors.add("No Answer svar signeringsTidpunkt elements found or set!");
            }
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
