/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;

public class RevokeMedicalCertificateResponderStub implements RevokeMedicalCertificateResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType attributedURIType,
        RevokeMedicalCertificateRequestType revokeMedicalCertificateRequestType) {
        CertificateHolder certResponseType = intygStore
            .getIntygForCertificateId(revokeMedicalCertificateRequestType.getRevoke().getLakarutlatande().getLakarutlatandeId());

        RevokeMedicalCertificateResponseType responseType = new RevokeMedicalCertificateResponseType();
        ResultOfCall resultOfCall = new ResultOfCall();

        if (certResponseType == null) {
            resultOfCall.setResultCode(ResultCodeEnum.ERROR);
            resultOfCall.setErrorId(ErrorIdEnum.APPLICATION_ERROR);
            responseType.setResult(resultOfCall);
            return responseType;
        }

        if (!isRevoked(certResponseType.getCertificateStates())) {
            intygStore.addStatus(certResponseType.getId(),
                new CertificateStateHolder(attributedURIType.getValue(), CertificateState.CANCELLED, LocalDateTime.now()));
        }

        resultOfCall.setResultCode(ResultCodeEnum.OK);
        responseType.setResult(resultOfCall);
        return responseType;
    }

    private boolean isRevoked(List<CertificateStateHolder> list) {
        return list.stream().filter(s -> CertificateState.CANCELLED.equals(s.getState())).findFirst().isPresent();
    }
}
