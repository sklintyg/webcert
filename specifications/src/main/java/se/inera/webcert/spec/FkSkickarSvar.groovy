package se.inera.webcert.spec

import org.springframework.core.io.ClassPathResource
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswer.rivtabp20.v1.ReceiveMedicalCertificateAnswerResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType
import se.inera.webcert.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller


import javax.xml.transform.stream.StreamSource

/**
 * @author andreaskaltenbach
 */
class FkSkickarSvar extends WsClientFixture {

    private def answerResponder

    String amne;
    String vardreferens
    String svarText
    String vardenhet

    public FkSkickarSvar() {
        super()
    }

    public FkSkickarSvar(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.receiveAnswerUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/receive-answer/v1.0"
        answerResponder = createClient(ReceiveMedicalCertificateAnswerResponderInterface.class, url)
    }

    public String resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateAnswerType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AnswerFromFkType answer = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("svar.xml").getInputStream()), AnswerFromFkType.class).getValue()
        answer.amne = amne
        answer.vardReferensId = vardreferens
        answer.svar.meddelandeText = svarText
        answer.adressVard.hosPersonal.enhet.enhetsId.extension = vardenhet

        def request = new ReceiveMedicalCertificateAnswerType();
        request.answer = answer

        def response = answerResponder.receiveMedicalCertificateAnswer(logicalAddress, request);
        resultAsString(response)
    }
}
