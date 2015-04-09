package se.inera.webcert.integration;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.integration.validator.QuestionAnswerValidator;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.mail.MailNotificationService;
import se.inera.webcert.service.notification.NotificationService;

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

        if (integreradeEnheterRegistry.isEnhetIntegrerad(careUnitId)) {
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
