package se.inera.webcert.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.logging.LogMarkers;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.webcert.service.fragasvar.FragaSvarService;

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

    @Override
    public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(
            AttributedURIType logicalAddress, ReceiveMedicalCertificateQuestionType request) {
        ReceiveMedicalCertificateQuestionResponseType response = new ReceiveMedicalCertificateQuestionResponseType();
        
        FragaSvar fragaSvar = converter.convert(request.getQuestion());
        
        LOGGER.info(LogMarkers.MONITORING, "Received question from '{}' with reference '{}'", fragaSvar.getFrageStallare(), fragaSvar.getExternReferens());
                
        fragaSvarService.processIncomingQuestion(fragaSvar);
        
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
