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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.inera.intygstjanster.ts.services.RegisterTSDiabetesResponder.v1.*;
import se.inera.intygstjanster.ts.services.v1.ResultCodeType;
import se.inera.intygstjanster.ts.services.v1.ResultatTyp;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class RegisterTSDiabetesResponderStub implements RegisterTSDiabetesResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RegisterTSDiabetesResponseType registerTSDiabetes(String logicalAddress, RegisterTSDiabetesType parameters) {

        intygStore.addIntyg(buildStubInternalCertificate(parameters));

        RegisterTSDiabetesResponseType resp = new RegisterTSDiabetesResponseType();
        ResultatTyp resultatTyp = new ResultatTyp();
        resultatTyp.setResultCode(ResultCodeType.OK);
        resp.setResultat(resultatTyp);
        return resp;
    }

    private CertificateHolder buildStubInternalCertificate(RegisterTSDiabetesType parameters) {
        CertificateHolder certificate = new CertificateHolder();
        certificate.setId(parameters.getIntyg().getIntygsId());
        certificate.setType(parameters.getIntyg().getIntygsTyp());
        certificate.setCivicRegistrationNumber(new Personnummer(parameters.getIntyg().getGrundData().getPatient().getPersonId().getExtension()));
        certificate.setSignedDate(LocalDateTime.parse(parameters.getIntyg().getGrundData().getSigneringsTidstampel(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        certificate.setCareUnitId(parameters.getIntyg().getGrundData().getSkapadAv().getVardenhet().getEnhetsId().getExtension());
        certificate.setCareUnitName(parameters.getIntyg().getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        certificate.setSigningDoctorName(parameters.getIntyg().getGrundData().getSkapadAv().getFullstandigtNamn());
        certificate.setAdditionalInfo(parameters.getIntyg().getOvrigKommentar());
        certificate.setCareGiverId(parameters.getIntyg().getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid().getExtension());
        return certificate;
    }
}
