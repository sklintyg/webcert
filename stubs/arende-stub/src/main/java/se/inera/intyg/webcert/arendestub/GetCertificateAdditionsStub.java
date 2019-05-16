/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.arendestub;

import riv.clinicalprocess.healthcond.certificate.getcertificateadditions._1.rivtabp21.GetCertificateAdditionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;

/**
 * @author Magnus Ekstrand on 2019-05-16.
 */
public class GetCertificateAdditionsStub implements
        GetCertificateAdditionsResponderInterface {

    @Override
    public GetCertificateAdditionsResponseType getCertificateAdditions(String logicalAddress, GetCertificateAdditionsType parameters) {
        return null;
    }
}
