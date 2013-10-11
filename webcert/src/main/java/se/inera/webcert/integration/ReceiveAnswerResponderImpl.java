package se.inera.webcert.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.service.FragaSvarService;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ReceiveAnswerResponderImpl implements ReceiveMedicalCertificateAnswerResponderInterface {

    @Autowired
    private FragaSvarService fragaSvarService;

    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType request) {
        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();

        if (request.getAnswer().getSvar() == null) {
            response.setResult(ResultOfCallUtil.failResult("Missing svar element."));
            return response;
        }

        InnehallType svar = request.getAnswer().getSvar();
        fragaSvarService.processIncomingAnswer(Long.parseLong(request.getAnswer().getVardReferensId()),
                svar.getMeddelandeText(), svar.getSigneringsTidpunkt());

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
