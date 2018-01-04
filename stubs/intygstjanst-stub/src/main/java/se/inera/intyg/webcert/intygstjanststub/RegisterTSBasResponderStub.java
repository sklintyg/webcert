/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.inera.intygstjanster.ts.services.RegisterTSBasResponder.v1.*;
import se.inera.intygstjanster.ts.services.v1.ResultCodeType;
import se.inera.intygstjanster.ts.services.v1.ResultatTyp;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class RegisterTSBasResponderStub implements RegisterTSBasResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RegisterTSBasResponseType registerTSBas(String logicalAddress, RegisterTSBasType parameters) {

        intygStore.addIntyg(buildStubInternalCertificate(parameters));

        RegisterTSBasResponseType resp = new RegisterTSBasResponseType();
        ResultatTyp resultatTyp = new ResultatTyp();
        resultatTyp.setResultCode(ResultCodeType.OK);
        resp.setResultat(resultatTyp);
        return resp;
    }

    private CertificateHolder buildStubInternalCertificate(RegisterTSBasType source) {
        CertificateHolder certificate = new CertificateHolder();
        certificate.setId(source.getIntyg().getIntygsId());
        certificate.setType(source.getIntyg().getIntygsTyp());
        certificate
                .setCivicRegistrationNumber(new Personnummer(source.getIntyg().getGrundData().getPatient().getPersonId().getExtension()));
        certificate.setSignedDate(
                LocalDateTime.parse(source.getIntyg().getGrundData().getSigneringsTidstampel(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        certificate.setCareUnitId(source.getIntyg().getGrundData().getSkapadAv().getVardenhet().getEnhetsId().getExtension());
        certificate.setCareUnitName(source.getIntyg().getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        certificate.setSigningDoctorName(source.getIntyg().getGrundData().getSkapadAv().getFullstandigtNamn());
        certificate.setAdditionalInfo(source.getIntyg().getOvrigKommentar());
        certificate.setCareGiverId(
                source.getIntyg().getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid().getExtension());
        return certificate;
    }
}
