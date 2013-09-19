package se.inera.webcert.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.persistence.FragaSvar;
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.service.FragaSvarService;

/**
 * @author andreaskaltenbach
 */
@Transactional
@SchemaValidation
public class ReceiveAnswerResponderImpl implements ReceiveMedicalCertificateAnswerResponderInterface {

    @Autowired
    private FragaSvarConverter converter;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType request) {
        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();

        FragaSvar fragaSvar = converter.convert(request.getAnswer());
        fragaSvarService.processIncomingAnswer(fragaSvar);

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
