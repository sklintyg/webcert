package se.inera.webcert.spec

import static groovyx.net.http.ContentType.JSON

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import org.springframework.core.io.ClassPathResource
import se.inera.webcert.spec.util.RestClientFixture

/**
 * Created by pehr on 9/23/13.
 */
public class FragaSvar extends RestClientFixture implements GroovyObject {

    String amne
    String externReferens
    String frageStallare
    String meddelandeRubrik
    String frageText
    String fragaSkickad

    String svarsText
    String svarSkickat

    String sistaDatumForSvar

    String intygsId
    String intygsTyp

    String lakareId
    String lakareNamn
    String enhetsId

    String status

    Boolean vidarebefordrad

    String internReferens

    String beskrivning

    public String internReferens() {
        internReferens
    }

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        def response = restClient.post(
                path: 'questions',
                body: questionAnswerJson(),
                requestContentType: JSON
        )
        internReferens = response.data.internReferens
    }

    private questionAnswerJson() {
        def fraga = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("fraga_svar_template.json").getInputStream()))

        if (amne) fraga.amne = amne
        if (externReferens) fraga.externReferens = externReferens
        fraga.meddelandeRubrik = meddelandeRubrik
        fraga.frageText = frageText
        if (fragaSkickad) fraga.frageSkickadDatum = fragaSkickad
        fraga.intygsReferens.intygsId = intygsId;
        if (intygsTyp) fraga.intygsReferens.intygsTyp = intygsTyp
        if (sistaDatumForSvar) fraga.sistaDatumForSvar = sistaDatumForSvar
        if (frageStallare) fraga.frageStallare = frageStallare
        if (status) fraga.status = status
        if (vidarebefordrad) fraga.vidarebefordrad = vidarebefordrad
        if (svarsText) fraga.svarsText = svarsText
        if (svarSkickat) fraga.svarSkickadDatum = svarSkickat

        if (lakareId) fraga.vardperson.hsaId = lakareId
        if (lakareNamn) fraga.vardperson.namn = lakareNamn
        if (enhetsId) fraga.vardperson.enhetsId = enhetsId

        JsonOutput.toJson(fraga)
    }
}