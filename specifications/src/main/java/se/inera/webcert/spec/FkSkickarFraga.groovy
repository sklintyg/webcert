package se.inera.webcert.spec

import org.springframework.core.io.ClassPathResource
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
import se.inera.webcert.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

/**
 * @author andreaskaltenbach
 */
class FkSkickarFraga extends WsClientFixture {

    private def questionResponder

    String amne
    String externReferens
    String frageText
    String intygsId
    String vardpersonal
    String vardenhet

    public FkSkickarFraga() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public FkSkickarFraga(String logiskAddress) {
        super(logiskAddress)
        String serviceUrl = System.getProperty("service.receiveQuestionUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "receive-question/v1.0"
        questionResponder = createClient(ReceiveMedicalCertificateQuestionResponderInterface.class, url)
    }

    def resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        def question = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fraga.xml").getInputStream()), QuestionFromFkType.class).getValue()
        question.amne = amne
        question.fkReferensId = externReferens
        question.fraga.meddelandeText = frageText
        question.lakarutlatande.lakarutlatandeId = intygsId
        question.adressVard.hosPersonal.personalId.extension = vardpersonal
        question.adressVard.hosPersonal.enhet.enhetsId.extension = vardenhet

        def request = new ReceiveMedicalCertificateQuestionType();
        request.question = question

        def response = questionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
        resultAsString(response)
    }
}
