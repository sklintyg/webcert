/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate;

// CHECKSTYLE:OFF LineLength

import com.google.common.base.Joiner;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswer.rivtabp20.v1.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

// CHECKSTYLE:ON LineLength

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ReceiveAnswerResponderImpl implements ReceiveMedicalCertificateAnswerResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveAnswerResponderImpl.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType request) {

        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();

        // Validate incoming request
        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(Joiner.on(",").join(validationMessages)));
            return response;
        }

        // Fetch the answer
        AnswerFromFkType answerType = request.getAnswer();

        // Verify there is a valid reference ID
        Long referensId;
        try {
            referensId = Long.parseLong(answerType.getVardReferensId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("No question found with internal ID " + request.getAnswer().getVardReferensId(), e);
        }

        // Set result and send response back to caller
        response.setResult(ResultOfCallUtil.okResult());

        // Notify stakeholders and return the response
        sendNotification(processAnswer(referensId, answerType.getSvar()));
        return response;
    }

    private FragaSvar processAnswer(Long referensId, InnehallType answerContents) {
        long refId = referensId;
        String text = answerContents.getMeddelandeText();
        LocalDateTime ldt = answerContents.getSigneringsTidpunkt();

        return fragaSvarService.processIncomingAnswer(refId, text, ldt);
    }

    private void sendNotification(FragaSvar fragaSvar) {
        notificationService.sendNotificationForAnswerRecieved(fragaSvar);
        LOGGER.debug("Notification sent: an answer with id '{}' (related to certificate with id '{}') was received from FK.",
                fragaSvar.getInternReferens(), fragaSvar.getIntygsReferens().getIntygsId());
    }
}
