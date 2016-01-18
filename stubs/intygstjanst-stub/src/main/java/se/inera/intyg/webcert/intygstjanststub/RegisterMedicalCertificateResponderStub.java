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
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.riv.clinicalprocess.healthcond.certificate.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v1.Utlatande;

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
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType attributedURIType, RegisterMedicalCertificateType registerMedicalCertificateType) {
        GetCertificateForCareResponseType request = new GetCertificateForCareResponseType();

        Utlatande certificate = buildStubInternalCertificate(registerMedicalCertificateType);
        CertificateMetaType meta = buildStubInternalMeta(registerMedicalCertificateType);

        request.setCertificate(certificate);
        request.setMeta(meta);
        intygStore.addIntyg(request);

        return buildResponse();
    }

    private RegisterMedicalCertificateResponseType buildResponse() {
        RegisterMedicalCertificateResponseType responseType = new RegisterMedicalCertificateResponseType();
        ResultOfCall resultOfCall = new ResultOfCall();
        resultOfCall.setResultCode(ResultCodeEnum.OK);
        responseType.setResult(resultOfCall);
        return responseType;
    }

    private Utlatande buildStubInternalCertificate(RegisterMedicalCertificateType registerMedicalCertificateType) {
        Utlatande certificate = new Utlatande();
        UtlatandeId utlatandeId = new UtlatandeId();
        utlatandeId.setExtension(registerMedicalCertificateType.getLakarutlatande().getLakarutlatandeId());

        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension(registerMedicalCertificateType.getLakarutlatande().getPatient().getPersonId().getExtension());
        patient.setPersonId(personId);

        certificate.setPatient(patient);
        certificate.setSigneringsdatum(LocalDateTime.now());
        certificate.setUtlatandeId(utlatandeId);
        return certificate;
    }

    private CertificateMetaType buildStubInternalMeta(RegisterMedicalCertificateType registerMedicalCertificateType) {
        CertificateMetaType meta = new CertificateMetaType();
        meta.setCertificateId(registerMedicalCertificateType.getLakarutlatande().getLakarutlatandeId());
        meta.setCertificateType(registerMedicalCertificateType.getLakarutlatande().getTypAvUtlatande());
        meta.setSignDate(registerMedicalCertificateType.getLakarutlatande().getSigneringsdatum());
        meta.setFacilityName(registerMedicalCertificateType.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getEnhetsnamn());
        meta.setIssuerName(registerMedicalCertificateType.getLakarutlatande().getSkapadAvHosPersonal().getFullstandigtNamn());
        meta.setComplemantaryInfo(registerMedicalCertificateType.getLakarutlatande().getKommentar());
        return meta;
    }
}
