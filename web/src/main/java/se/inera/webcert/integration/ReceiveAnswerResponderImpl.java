package se.inera.webcert.integration;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.logging.LogMarkers;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.integration.validator.QuestionAnswerValidator;

import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.mail.MailNotificationService;

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

    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType request) {

        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();

        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(StringUtils.join(validationMessages, ",")));
            return response;
        }

        AnswerFromFkType answerType = request.getAnswer();

        LOGGER.info(LogMarkers.MONITORING, "Recieved answer to question '{}'", answerType.getVardReferensId());

        InnehallType answerContents = answerType.getSvar();

        long referensId;
        try {
            referensId = Long.parseLong(answerType.getVardReferensId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("No question found with internal ID " + answerType.getVardReferensId(), e);
        }

        FragaSvar fragaSvar = fragaSvarService.processIncomingAnswer(referensId, answerContents.getMeddelandeText(), answerContents.getSigneringsTidpunkt());

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingAnswer(fragaSvar);
        } catch (MailSendException e) {
            Long svarsId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOGGER.error("Notification mail for answer '" + svarsId
                    +  "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
