/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

import java.util.List;

// CHECKSTYLE:ON LineLength

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ReceiveQuestionResponderImpl implements ReceiveMedicalCertificateQuestionResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveQuestionResponderImpl.class);

    @Autowired
    private FragaSvarConverter converter;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateQuestionType request) {

        ReceiveMedicalCertificateQuestionResponseType response = new ReceiveMedicalCertificateQuestionResponseType();

        // Validate incoming request
        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(Joiner.on(",").join(validationMessages)));
            return response;
        }

        // Transform to a FragaSvar object
        FragaSvar fragaSvar = converter.convert(request.getQuestion());

        // Notify stakeholders
        sendNotification(processQuestion(fragaSvar));

        // Set result and send response back to caller
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private FragaSvar processQuestion(FragaSvar fragaSvar) {
        return fragaSvarService.processIncomingQuestion(fragaSvar);
    }

    private void sendNotification(FragaSvar fragaSvar) {
        notificationService.sendNotificationForQuestionReceived(fragaSvar);
        LOGGER.debug("Notification sent: a question with id '{}' (related to certificate with id '{}') was received from FK.",
                fragaSvar.getInternReferens(), fragaSvar.getIntygsReferens().getIntygsId());
    }

}
