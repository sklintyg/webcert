package se.inera.webcert.spec

import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
import se.inera.webcert.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
class ValideraFraga extends WsClientFixture {

    ReceiveMedicalCertificateQuestionResponderInterface receiveMedicalCertificateQuestionResponder

    public ValideraFraga() {
        super()
    }
    
    public ValideraFraga(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = baseUrl + "services/receive-question/v1.0"
        receiveMedicalCertificateQuestionResponder = createClient(ReceiveMedicalCertificateQuestionResponderInterface.class, url)
    }

    String filnamn
    
    ReceiveMedicalCertificateQuestionResponseType response

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        ReceiveMedicalCertificateQuestionType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        ReceiveMedicalCertificateQuestionType.class).getValue()

        response = receiveMedicalCertificateQuestionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
