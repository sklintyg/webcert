/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import org.springframework.core.io.ClassPathResource
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswer.rivtabp20.v1.ReceiveMedicalCertificateAnswerResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType
import se.inera.intyg.webcert.specifications.spec.util.WsClientFixture

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
