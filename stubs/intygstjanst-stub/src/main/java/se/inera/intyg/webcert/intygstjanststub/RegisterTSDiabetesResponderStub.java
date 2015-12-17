/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intygstjanster.ts.services.RegisterTSDiabetesResponder.v1.RegisterTSDiabetesResponderInterface;
import se.inera.intygstjanster.ts.services.RegisterTSDiabetesResponder.v1.RegisterTSDiabetesResponseType;
import se.inera.intygstjanster.ts.services.RegisterTSDiabetesResponder.v1.RegisterTSDiabetesType;
import se.inera.intygstjanster.ts.services.v1.ResultCodeType;
import se.inera.intygstjanster.ts.services.v1.ResultatTyp;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.riv.clinicalprocess.healthcond.certificate.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v1.Utlatande;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class RegisterTSDiabetesResponderStub implements RegisterTSDiabetesResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RegisterTSDiabetesResponseType registerTSDiabetes(String logicalAddress, RegisterTSDiabetesType parameters) {

        GetCertificateForCareResponseType req = new GetCertificateForCareResponseType();
        Utlatande cert = new Utlatande();
        UtlatandeId utlatandeId = new UtlatandeId();
        utlatandeId.setExtension(parameters.getIntyg().getIntygsId());
        TypAvUtlatande typAvUtlatande = new TypAvUtlatande();
        typAvUtlatande.setCode(parameters.getIntyg().getIntygsTyp());

        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension(parameters.getIntyg().getGrundData().getPatient().getPersonId().getExtension());

        patient.setPersonId(personId);
        cert.setPatient(patient);
        cert.setTypAvUtlatande(typAvUtlatande);
        cert.setUtlatandeId(utlatandeId);
        req.setCertificate(cert);

        CertificateMetaType certificateMetaType = buildStubInternalMeta(parameters);
        req.setMeta(certificateMetaType);

        intygStore.addIntyg(req);

        RegisterTSDiabetesResponseType resp = new RegisterTSDiabetesResponseType();
        ResultatTyp resultatTyp = new ResultatTyp();
        resultatTyp.setResultCode(ResultCodeType.OK);
        resp.setResultat(resultatTyp);
        return resp;
    }

    private CertificateMetaType buildStubInternalMeta(RegisterTSDiabetesType parameters) {
        CertificateMetaType meta = new CertificateMetaType();
        meta.setCertificateId(parameters.getIntyg().getIntygsId());
        meta.setCertificateType(parameters.getIntyg().getIntygsTyp());

        return meta;
    }
}
