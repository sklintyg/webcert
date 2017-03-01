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

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;

/**
 * Note: This stub populates the in-memory store with a small subset of the certificate data.
 *
 * Created by eriklupander on 2015-06-10.
 */
public class RegisterMedicalCertificateResponderStub implements RegisterMedicalCertificateResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType attributedURIType,
            RegisterMedicalCertificateType registerMedicalCertificateType) {

        intygStore.addIntyg(buildStubInternalCertificate(registerMedicalCertificateType));

        return buildResponse();
    }

    private RegisterMedicalCertificateResponseType buildResponse() {
        RegisterMedicalCertificateResponseType responseType = new RegisterMedicalCertificateResponseType();
        ResultOfCall resultOfCall = new ResultOfCall();
        resultOfCall.setResultCode(ResultCodeEnum.OK);
        responseType.setResult(resultOfCall);
        return responseType;
    }

    private CertificateHolder buildStubInternalCertificate(RegisterMedicalCertificateType source) {
        CertificateHolder certificate = new CertificateHolder();
        certificate.setId(source.getLakarutlatande().getLakarutlatandeId());
        certificate.setType(source.getLakarutlatande().getTypAvUtlatande());
        certificate.setCivicRegistrationNumber(new Personnummer(source.getLakarutlatande().getPatient().getPersonId().getExtension()));
        certificate.setSignedDate(source.getLakarutlatande().getSigneringsdatum());
        certificate.setCareUnitId(source.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getEnhetsId().getExtension());
        certificate.setCareUnitName(source.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getEnhetsnamn());
        certificate.setSigningDoctorName(source.getLakarutlatande().getSkapadAvHosPersonal().getFullstandigtNamn());
        certificate.setAdditionalInfo(source.getLakarutlatande().getKommentar());
        certificate.setCareGiverId(
                source.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getVardgivare().getVardgivareId().getExtension());
        return certificate;
    }
}
