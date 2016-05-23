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

package se.inera.intyg.webcert.web.integration;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.validator.QuestionAnswerValidator;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

import java.util.List;


/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ReceiveQuestionResponderImpl implements ReceiveMedicalCertificateQuestionResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveQuestionResponderImpl.class);

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarConverter converter;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Override
    public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateQuestionType request) {

        ReceiveMedicalCertificateQuestionResponseType response = new ReceiveMedicalCertificateQuestionResponseType();

        // Validate incoming request
        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(StringUtils.join(validationMessages, ",")));
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
        FragaSvar fs = fragaSvarService.processIncomingQuestion(fragaSvar);
        return fs;
    }

    private void sendNotification(FragaSvar fragaSvar) {

        String careUnitId = fragaSvar.getVardperson().getEnhetsId();

        if (integreradeEnheterRegistry.isEnhetIntegrerad(careUnitId, Fk7263EntryPoint.MODULE_ID)) {
            sendNotificationToQueue(fragaSvar);
        } else {
            sendNotificationByMail(fragaSvar);
        }
    }

    private void sendNotificationToQueue(FragaSvar fragaSvar) {

        if (fragaSvar.getStatus() == Status.CLOSED) {
            notificationService.sendNotificationForQuestionHandled(fragaSvar);
            LOGGER.debug("Notification sent: a closed question with id '{}' (related to certificate with id '{}') was received from FK.",
                    fragaSvar.getInternReferens(), fragaSvar.getIntygsReferens().getIntygsId());
        } else {
            notificationService.sendNotificationForQuestionReceived(fragaSvar);
            LOGGER.debug("Notification sent: a question with id '{}' (related to certificate with id '{}') was received from FK.",
                    fragaSvar.getInternReferens(), fragaSvar.getIntygsReferens().getIntygsId());
        }

    }

    private void sendNotificationByMail(FragaSvar fragaSvar) {
        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (MailSendException e) {
            Long frageId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOGGER.error("Notification mail for question '" + frageId
                    + "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }

}
