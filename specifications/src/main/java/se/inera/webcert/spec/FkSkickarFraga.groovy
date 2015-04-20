package se.inera.webcert.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
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
    String rubrik
    String falt
    String komplettering
    String signeringsTidpunkt
    String avsantTidpunkt
    String intygsId
    String patientId
    String patientNamn
    String vardpersonal
    String vardpersonalNamn
    String forskrivarKod
    String vardenhet
    String vardenhetNamn
    String arbetsplatsKod
    String vardgivare
    String vardgivarNamn
    
    public FkSkickarFraga() {
        super()
    }

    public FkSkickarFraga(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.receiveQuestionUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/receive-question/v1.0"
        questionResponder = createClient(ReceiveMedicalCertificateQuestionResponderInterface.class, url)
    }

    public void reset() {
        amne = null
        externReferens = null
        frageText = null
        rubrik = null
        falt = null
        komplettering = null
        signeringsTidpunkt = null
        avsantTidpunkt = null
        intygsId = null
        patientId = null
        patientNamn = null
        vardpersonal = null
        vardpersonalNamn = null
        forskrivarKod = null
        vardenhet = null
        vardenhetNamn = null
        arbetsplatsKod = null
        vardgivare = null
        vardgivarNamn = null
    }

    def resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        def question = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fraga.xml").getInputStream()), QuestionFromFkType.class).getValue()
        question.amne = amne
        if (rubrik) question.fkMeddelanderubrik = rubrik
        question.fkReferensId = externReferens
        question.fraga.meddelandeText = frageText
        if (falt) question.fkKomplettering[0].falt = falt
        if (komplettering) question.fkKomplettering[0].text = komplettering
        if (!signeringsTidpunkt) signeringsTidpunkt = LocalDateTime.now().toString()
        question.fraga.signeringsTidpunkt = LocalDateTime.parse(signeringsTidpunkt)
        if (!avsantTidpunkt) avsantTidpunkt = LocalDateTime.now().toString()
        question.avsantTidpunkt = LocalDateTime.parse(avsantTidpunkt)
        question.lakarutlatande.lakarutlatandeId = intygsId
        if (patientId) question.lakarutlatande.patient.personId.extension = patientId
        if (patientNamn) question.lakarutlatande.patient.fullstandigtNamn = patientNamn
        question.adressVard.hosPersonal.personalId.extension = vardpersonal
        if (vardpersonalNamn) question.adressVard.hosPersonal.fullstandigtNamn = vardpersonalNamn
        if (forskrivarKod) question.adressVard.hosPersonal.forskrivarkod = forskrivarKod
        question.adressVard.hosPersonal.enhet.enhetsId.extension = vardenhet
        if (vardenhetNamn) question.adressVard.hosPersonal.enhet.enhetsnamn = vardenhetNamn
        if (arbetsplatsKod) question.adressVard.hosPersonal.enhet.arbetsplatskod.extension = arbetsplatsKod
        if (vardgivare) question.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.extension = vardgivare
        if (vardgivarNamn) question.adressVard.hosPersonal.enhet.vardgivare.vardgivarnamn = vardgivarNamn
        
        def request = new ReceiveMedicalCertificateQuestionType();
        request.question = question

        def response = questionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
        resultAsString(response)
    }
}
