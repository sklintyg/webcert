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

package se.inera.intyg.webcert.intygstjanststub;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.riv.clinicalprocess.healthcond.certificate.v1.StatusType;
import se.riv.clinicalprocess.healthcond.certificate.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.v1.UtlatandeStatus;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class SendCertificateToRecipientResponderStub implements SendCertificateToRecipientResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public SendCertificateToRecipientResponseType sendCertificateToRecipient(String logicalAddress, SendCertificateToRecipientType parameters) {
        GetCertificateForCareResponseType fromStore = intygStore.getIntygForCertificateId(parameters.getUtlatandeId());

        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        if (fromStore == null) {
            ResultType resultOfCall = new ResultType();
            resultOfCall.setResultCode(ResultCodeType.ERROR);
            resultOfCall.setErrorId(ErrorIdType.APPLICATION_ERROR);
            responseType.setResult(resultOfCall);
            return responseType;
        }

        Utlatande intyg = fromStore.getCertificate();
        intyg.setSkickatdatum(LocalDateTime.now());
        intygStore.updateUtlatande(intyg);

        UtlatandeStatus sentStatus = new UtlatandeStatus();
        sentStatus.setTarget("FK");
        sentStatus.setTimestamp(LocalDateTime.now());
        sentStatus.setType(StatusType.SENT);
        intygStore.addStatus(parameters.getUtlatandeId(), sentStatus);

        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        responseType.setResult(resultType);
        return responseType;
    }
}
