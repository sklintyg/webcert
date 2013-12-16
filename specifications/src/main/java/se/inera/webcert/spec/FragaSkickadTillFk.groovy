package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import org.springframework.core.io.ClassPathResource
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource
/**
 *
 * @author pehra
 */
class FragaSkickadTillFk extends RestClientFixture implements  GroovyObject {

    private def questionResponder


    def fragaJson

    public String fraga(){
            fragaJson.fraga.meddelandeText[0]
    }
    
    public String internReferens(){
        fragaJson.vardReferensId[0]
    }

    public void execute() {
        def restClient = new RESTClient(baseUrl)


        fragaJson = restClient.get(path: "fk-stub/fragor/").data
    }


}
