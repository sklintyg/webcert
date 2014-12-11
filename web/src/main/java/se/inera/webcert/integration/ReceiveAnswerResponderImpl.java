package se.inera.webcert.integration;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.logging.LogMarkers;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.integration.validator.QuestionAnswerValidator;

import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.mail.MailNotificationService;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ReceiveAnswerResponderImpl implements ReceiveMedicalCertificateAnswerResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveAnswerResponderImpl.class);

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType request) {

        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();

        // Validate incoming request
        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(StringUtils.join(validationMessages, ",")));
            return response;
        }

        // Fetch the answer
        AnswerFromFkType answerType = request.getAnswer();

        // Verify there is a valid reference ID
        Long referensId = null;
        try {
            referensId = Long.parseLong(answerType.getVardReferensId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("No question found with internal ID " + request.getAnswer().getVardReferensId(), e);
        }

        LOGGER.info(LogMarkers.MONITORING, "Received answer to question '{}'", referensId);

        // Notify stakeholders
        notify(processAnswer(referensId, answerType.getSvar()));

        // Set result and send response back to caller
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private FragaSvar processAnswer(Long referensId, InnehallType answerContents) {
        long refId = referensId.longValue();
        String text = answerContents.getMeddelandeText();
        LocalDateTime ldt = answerContents.getSigneringsTidpunkt();

        FragaSvar fragaSvar = fragaSvarService.processIncomingAnswer(refId, text, ldt);
        return fragaSvar;
    }

    private void notify(FragaSvar fragaSvar) {

        String careUnitId = fragaSvar.getVardperson().getEnhetsId();

        if (integreradeEnheterRegistry.isEnhetIntegrerad(careUnitId)) {
            sendNotificationToQueue(fragaSvar);
        } else {
            sendNotificationByMail(fragaSvar);
        }
    }

    private void sendNotificationToQueue(FragaSvar fragaSvar) {
        NotificationRequestType notificationRequestType = null;

        if (fragaSvar.getStatus() == Status.CLOSED) {
            notificationRequestType = NotificationMessageFactory.createNotificationFromClosedAnswerFromFK(fragaSvar);
        } else {
            notificationRequestType = NotificationMessageFactory.createNotificationFromAnswerFromFK(fragaSvar);
        }

        notificationService.notify(notificationRequestType);
    }

    private void sendNotificationByMail(FragaSvar fragaSvar) {
        // Send mail to enhet to inform about new answer
        try {
            mailNotificationService.sendMailForIncomingAnswer(fragaSvar);
        } catch (MailSendException e) {
            Long svarsId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOGGER.error("Notification mail for answer '" + svarsId
                    + "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }
}
