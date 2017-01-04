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

import se.inera.intyg.webcert.specifications.spec.util.FitnesseHelper
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType
import se.inera.intyg.webcert.specifications.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
class ValideraFraga extends WsClientFixture {

    ReceiveMedicalCertificateQuestionResponderInterface receiveMedicalCertificateQuestionResponder

    public ValideraFraga() {
        super()
    }

    public ValideraFraga(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = baseUrl + "services/receive-question/v1.0"
        receiveMedicalCertificateQuestionResponder = createClient(ReceiveMedicalCertificateQuestionResponderInterface.class, url)
    }

    String filnamn

    ReceiveMedicalCertificateQuestionResponseType response

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        ReceiveMedicalCertificateQuestionType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        ReceiveMedicalCertificateQuestionType.class).getValue()

        response = receiveMedicalCertificateQuestionResponder.receiveMedicalCertificateQuestion(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
