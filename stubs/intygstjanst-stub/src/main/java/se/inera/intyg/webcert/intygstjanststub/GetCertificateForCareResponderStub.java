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

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Stub class for mocking Intygstjanstens {@link GetCertificateForCareResponderStub} WS interface Uses a simple in
 * memory store for complete {@link GetCertificateForCareResponseType} responses.
 *
 * @author marced
 */
public class GetCertificateForCareResponderStub implements GetCertificateForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public GetCertificateForCareResponseType getCertificateForCare(String logicalAddress,
                                                                   GetCertificateForCareRequestType request) {

        GetCertificateForCareResponseType intygResponse = intygStore.getAllIntyg().get(request.getCertificateId());
        if (intygResponse != null) {
            return intygResponse;
        } else {
            return buildNotFoundResponse();
        }
    }

    private GetCertificateForCareResponseType buildNotFoundResponse() {
        GetCertificateForCareResponseType response = new GetCertificateForCareResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.ERROR);
        resultType.setErrorId(ErrorIdType.VALIDATION_ERROR);
        resultType.setResultText("Intyg not found in stub");
        response.setResult(resultType);
        return response;

    }

}
