/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.spec.api

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class FragaSvar extends RestClientFixture {

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
    String patientId

    String status
    String fkKontakt

    Boolean vidarebefordrad

    String internReferens

    String beskrivning

    String vardAktorHsaId

    String vardAktorNamn

    // Komplettering
    String falt
    String text

    Komplettering komplettering

    String internReferens() {
        internReferens
    }

    def execute() {
        def restClient = createRestClient("${baseUrl}testability/")
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
        if (patientId) fraga.intygsReferens.patientId = patientId
        if (sistaDatumForSvar) fraga.sistaDatumForSvar = sistaDatumForSvar
        if (frageStallare) fraga.frageStallare = frageStallare
        if (status) fraga.status = status
        if (vidarebefordrad) fraga.vidarebefordrad = vidarebefordrad
        if (svarsText) fraga.svarsText = svarsText
        if (svarSkickat) fraga.svarSkickadDatum = svarSkickat

        if (lakareId) fraga.vardperson.hsaId = lakareId
        if (lakareNamn) fraga.vardperson.namn = lakareNamn
        if (enhetsId) fraga.vardperson.enhetsId = enhetsId
        if (vardAktorHsaId) fraga.vardAktorHsaId = vardAktorHsaId
        if (vardAktorNamn) fraga.vardAktorNamn = vardAktorNamn
        if (fkKontakt) fraga.externaKontakter = [fkKontakt]

        if (falt && text) {
            komplettering = new Komplettering(falt, text)
            fraga.kompletteringar = [komplettering]
        }
        if (text) fraga.text = text

        JsonOutput.toJson(fraga)
    }

    class Komplettering {
        String falt
        String text

        Komplettering(String falt, String text) {
            this.falt = falt
            this.text = text
        }
    }
}
