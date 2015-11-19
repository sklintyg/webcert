package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intygstjanster.ts.services.RegisterTSBasResponder.v1.RegisterTSBasResponderInterface;
import se.inera.intygstjanster.ts.services.RegisterTSBasResponder.v1.RegisterTSBasResponseType;
import se.inera.intygstjanster.ts.services.RegisterTSBasResponder.v1.RegisterTSBasType;
import se.inera.intygstjanster.ts.services.v1.ResultCodeType;
import se.inera.intygstjanster.ts.services.v1.ResultatTyp;
import se.inera.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.riv.clinicalprocess.healthcond.certificate.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v1.Utlatande;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class RegisterTSBasResponderStub implements RegisterTSBasResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RegisterTSBasResponseType registerTSBas(String logicalAddress, RegisterTSBasType parameters) {

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

        RegisterTSBasResponseType resp = new RegisterTSBasResponseType();
        ResultatTyp resultatTyp = new ResultatTyp();
        resultatTyp.setResultCode(ResultCodeType.OK);
        resp.setResultat(resultatTyp);
        return resp;
    }

    private CertificateMetaType buildStubInternalMeta(RegisterTSBasType parameters) {
        CertificateMetaType meta = new CertificateMetaType();
        meta.setCertificateId(parameters.getIntyg().getIntygsId());
        meta.setCertificateType(parameters.getIntyg().getIntygsTyp());

        return meta;
    }
}
