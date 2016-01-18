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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

import com.google.common.collect.Lists;

/**
 * @author andreaskaltenbach
 */
public class ListCertificatesForCareResponderStub implements ListCertificatesForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public ListCertificatesForCareResponseType listCertificatesForCare(String s, ListCertificatesForCareType request) {

        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();

        Iterable<CertificateMetaType> certsIterable = intygStore.getIntygForEnhetAndPersonnummer(request.getEnhet(), request.getPersonId());

        List<CertificateMetaType> certs = Lists.newArrayList(certsIterable);
        response.getMeta().addAll(certs);

        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        response.setResult(result);

        return response;
    }
}
