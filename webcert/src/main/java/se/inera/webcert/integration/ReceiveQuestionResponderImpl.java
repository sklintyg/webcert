package se.inera.webcert.integration;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.logging.LogMarkers;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.integration.validator.QuestionAnswerValidator;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.service.mail.MailNotificationService;

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

    @Override
    public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateQuestionType request) {
        ReceiveMedicalCertificateQuestionResponseType response = new ReceiveMedicalCertificateQuestionResponseType();

        List<String> validationMessages = QuestionAnswerValidator.validate(request);
        if (!validationMessages.isEmpty()) {
            response.setResult(ResultOfCallUtil.failResult(StringUtils.join(validationMessages, ",")));
            return response;
        }

        FragaSvar fragaSvar = converter.convert(request.getQuestion());

        LOGGER.info(LogMarkers.MONITORING, "Received question from '{}' with reference '{}'", fragaSvar.getFrageStallare(), fragaSvar.getExternReferens());

        fragaSvar = fragaSvarService.processIncomingQuestion(fragaSvar);

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (MailSendException e) {
            Long frageId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOGGER.error("Notification mail for question '" + frageId
                      +  "' concerning certificate '" + intygsId
                      + "' couldn't be sent to " + enhetsId
                      + " (" + enhetsNamn + "): " + e.getMessage());
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
