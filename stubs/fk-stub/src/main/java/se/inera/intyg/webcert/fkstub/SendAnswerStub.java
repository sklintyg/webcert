/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.fkstub;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.webcert.fkstub.validation.SendMedicalCertificateAnswerValidator;
import se.inera.intyg.webcert.fkstub.validation.ValidationException;


/**
 * @author andreaskaltenbach
 */
public class SendAnswerStub implements SendMedicalCertificateAnswerResponderInterface {

    private static final String LOGICAL_ADDRESS = "SendAnswerStub";

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @Override
    public SendMedicalCertificateAnswerResponseType sendMedicalCertificateAnswer(AttributedURIType logicalAddress,
            SendMedicalCertificateAnswerType parameters) {
        SendMedicalCertificateAnswerResponseType response = new SendMedicalCertificateAnswerResponseType();

        if (logicalAddress == null) {
            response.setResult(ResultOfCallUtil.failResult("Ingen LogicalAddress är satt"));
        } else if (!LOGICAL_ADDRESS.equals(logicalAddress.getValue())) {
            response.setResult(ResultOfCallUtil.failResult("LogicalAddress '" + logicalAddress.getValue() + "' är inte samma som '" + LOGICAL_ADDRESS + "'"));
        } else if (parameters.getAnswer().getSvar().getMeddelandeText().equalsIgnoreCase("error")) {
            response.setResult(ResultOfCallUtil.failResult("Du ville ju få ett fel"));
        } else {
            AnswerToFkType answerType = parameters.getAnswer();
            SendMedicalCertificateAnswerValidator validator = new SendMedicalCertificateAnswerValidator(answerType);
            try {
                validator.validateAndCorrect();
                response.setResult(ResultOfCallUtil.okResult());
            } catch (ValidationException e) {
                response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            }
            questionAnswerStore.addAnswer(parameters.getAnswer());
        }

        return response;
    }
}
