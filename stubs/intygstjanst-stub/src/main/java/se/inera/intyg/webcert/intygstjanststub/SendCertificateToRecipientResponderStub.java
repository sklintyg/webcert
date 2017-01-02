/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

/**
 * Created by eriklupander on 2015-06-10.
 */
@SchemaValidation
public class SendCertificateToRecipientResponderStub implements SendCertificateToRecipientResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public SendCertificateToRecipientResponseType sendCertificateToRecipient(String logicalAddress, SendCertificateToRecipientType parameters) {
        CertificateHolder fromStore = intygStore.getIntygForCertificateId(parameters.getIntygsId().getExtension());

        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        if (fromStore == null) {
            ResultType resultOfCall = new ResultType();
            resultOfCall.setResultCode(ResultCodeType.ERROR);
            resultOfCall.setErrorId(ErrorIdType.APPLICATION_ERROR);
            responseType.setResult(resultOfCall);
            return responseType;
        }

        intygStore.addStatus(parameters.getIntygsId().getExtension(), new CertificateStateHolder("FK", CertificateState.SENT, LocalDateTime.now()));

        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        responseType.setResult(resultType);
        return responseType;
    }
}
