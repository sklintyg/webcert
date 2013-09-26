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
public class Fraga extends RestClientFixture {

    String amne
    String externReferens
    String frageStallare
    String meddelandeRubrik
    String frageText
    String frageSigneringsDatum
    String frageSkickadDatum
    String sistaDatumForSvar
    String externaKontakter

    String intygsId
    String intygsTyp

    String enhetsId
    String status
    Boolean vidarebefordrad

    String vardperson_mall
    String fraga_mall

    String internReferens

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        def response = restClient.post(
                path: 'questions',
                body: questionJson(),
                requestContentType: JSON
        )
        internReferens = response.data.internReferens
    }

    public String internReferens() {
        internReferens
    }

    private questionJson() {
        def fraga = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("fraga_${fraga_mall}_template.json").getInputStream()))

        fraga.vardperson = vardperson();
        fraga.vardperson.enhetsId = enhetsId;
        fraga.amne = amne;
        fraga.externReferens = externReferens;
        fraga.meddelandeRubrik = meddelandeRubrik;
        fraga.frageText = frageText;
        fraga.intygsReferens.intygsId = intygsId;
        fraga.intygsReferens.intygsTyp = intygsTyp;
        fraga.sistaDatumForSvar = sistaDatumForSvar;
        if (frageStallare != null) fraga.frageStallare = frageStallare
        if (status != null) fraga.status = status;
        if (vidarebefordrad != null) fraga.vidarebefordrad = vidarebefordrad;

        JsonOutput.toJson(fraga)
    }


    protected vardperson() {
        // slurping the vardperson template
        def vardperson = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("vardperson_${vardperson_mall}_template.json").getInputStream()))


    }

}

