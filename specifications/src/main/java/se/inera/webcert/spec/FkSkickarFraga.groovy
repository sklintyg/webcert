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
    String patientId
    String patientNamn
    String vardpersonal
    String vardpersonalNamn
    String vardenhet
    String vardgivare
    
    public FkSkickarFraga() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public FkSkickarFraga(String logiskAddress) {
        super(logiskAddress)
        String serviceUrl = System.getProperty("service.receiveQuestionUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "receive-question/v1.0"
        questionResponder = createClient(ReceiveMedicalCertificateQuestionResponderInterface.class, url)
    }

    public void reset() {
        amne = null
        externReferens = null
        frageText = null
        intygsId = null
        patientId = null
        patientNamn = null
        vardpersonal = null
        vardpersonalNamn = null
        vardenhet = null
        vardgivare = null
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
        if (patientId) question.lakarutlatande.patient.personId.extension = patientId
        if (patientNamn) question.lakarutlatande.patient.fullstandigtNamn = patientNamn
        question.adressVard.hosPersonal.personalId.extension = vardpersonal
        if (vardpersonalNamn) question.adressVard.hosPersonal.fullstandigtNamn = vardpersonalNamn
        question.adressVard.hosPersonal.enhet.enhetsId.extension = vardenhet
        if (vardgivare) question.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.extension = vardgivare

        def request = new ReceiveMedicalCertificateQuestionType();
        request.question = question

        def response = questionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
        resultAsString(response)
    }
}
