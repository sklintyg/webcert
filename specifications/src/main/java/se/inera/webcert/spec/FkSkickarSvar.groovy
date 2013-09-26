package se.inera.webcert.spec

import org.springframework.core.io.ClassPathResource
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
import se.inera.webcert.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

/**
 *
 * @author andreaskaltenbach
 */
class FkSkickarSvar extends WsClientFixture {

    private ReceiveMedicalCertificateAnswerResponderInterface questionResponder

    String amne;
    String externReferens;
    String frageText;
    String intygsId;
    String vardpersonal;
    String vardenhet;

    public FkSkickarFraga() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public FkSkickarFraga(String logiskAddress) {
        super(logiskAddress)
        String url = baseUrl + "receive-answer/v1.0"
        answerResponder = createClient(ReceiveMedicalCertificateAnswerResponderInterface.class, url)
    }

    public String resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        QuestionFromFkType question = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fraga.xml").getInputStream()), QuestionFromFkType.class).getValue()
        question.amne = amne
        question.fkReferensId = externReferens
        question.fraga.meddelandeText = frageText
        question.lakarutlatande.lakarutlatandeId = intygsId
        question.adressVard.hosPersonal.personalId.extension = vardpersonal
        question.adressVard.hosPersonal.enhet.enhetsId.extension = vardenhet

        ReceiveMedicalCertificateQuestionType request = new ReceiveMedicalCertificateQuestionType();
        request.question = question

        ReceiveMedicalCertificateQuestionResponseType response = questionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
        resultAsString(response)
    }
