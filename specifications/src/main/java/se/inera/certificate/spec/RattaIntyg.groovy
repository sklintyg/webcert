package se.inera.certificate.spec

import iso.v21090.dt.v1.II
import org.joda.time.LocalDateTime
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType


/**
 *
 * @author andreaskaltenbach
 */
class RattaIntyg extends WsClientFixture {

    protected RevokeMedicalCertificateResponderInterface revokeResponder

	static String serviceUrl = System.getProperty("service.revokeCertificateUrl")

    private static final String PATIENT_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String HOS_PERSONAL_OID = "1.2.752.129.2.1.4.1";
    private static final String ENHET_OID = "1.2.752.129.2.1.4.1";
    private static final String ARBETSPLATS_CODE_OID = "1.2.752.29.4.71";

    String personnummer
    String intyg
    String meddelande

    public RattaIntyg() {
        super()
    }

    public RattaIntyg(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "revoke-certificate/v1.0"
		revokeResponder = createClient(RevokeMedicalCertificateResponderInterface.class, url)
    }

    public void reset() {
        meddelande = null
    }

    public String resultat() {
        RevokeMedicalCertificateRequestType revokeRequestType = new RevokeMedicalCertificateRequestType()
        RevokeType revokeType = new RevokeType();
        revokeRequestType.setRevoke(revokeType)
        revokeType.vardReferensId = 1
        revokeType.avsantTidpunkt = new LocalDateTime("2013-05-01T11:00:00")
        revokeType.meddelande = meddelande
        revokeType.adressVard = new VardAdresseringsType()
        revokeType.adressVard.hosPersonal = new HosPersonalType()
        revokeType.adressVard.hosPersonal.fullstandigtNamn = "MI"
        revokeType.adressVard.hosPersonal.personalId = new II()
        revokeType.adressVard.hosPersonal.personalId.root = HOS_PERSONAL_OID;
        revokeType.adressVard.hosPersonal.personalId.extension = "personalid"

        revokeType.adressVard.hosPersonal.enhet = new EnhetType()
        revokeType.adressVard.hosPersonal.enhet.enhetsId = new II()
        revokeType.adressVard.hosPersonal.enhet.enhetsId.root = ENHET_OID
        revokeType.adressVard.hosPersonal.enhet.enhetsId.extension = "1"
        revokeType.adressVard.hosPersonal.enhet.enhetsnamn = "Enhetsnamn"
        revokeType.adressVard.hosPersonal.enhet.vardgivare = new VardgivareType()
        revokeType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId = new II()
        revokeType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.root = ENHET_OID
        revokeType.adressVard.hosPersonal.enhet.vardgivare.vardgivareId.extension = ENHET_OID
        revokeType.adressVard.hosPersonal.enhet.vardgivare.vardgivarnamn = "Vårdgivarnamn"

        revokeType.adressVard.hosPersonal.enhet.arbetsplatskod = new II()
        revokeType.adressVard.hosPersonal.enhet.arbetsplatskod.root = ARBETSPLATS_CODE_OID
        revokeType.adressVard.hosPersonal.enhet.arbetsplatskod.extension = ARBETSPLATS_CODE_OID

        revokeType.lakarutlatande = new LakarutlatandeEnkelType()
        revokeType.lakarutlatande.lakarutlatandeId = intyg
        revokeType.lakarutlatande.signeringsTidpunkt = new LocalDateTime("2013-05-01T11:00:00")
        revokeType.lakarutlatande.patient = new PatientType()
        revokeType.lakarutlatande.patient.fullstandigtNamn = "Test Testsson"
        revokeType.lakarutlatande.patient.personId = new II()
        revokeType.lakarutlatande.patient.personId.root = PATIENT_ID_OID
        revokeType.lakarutlatande.patient.personId.extension = personnummer


        RevokeMedicalCertificateResponseType response = revokeResponder.revokeMedicalCertificate(logicalAddress, revokeRequestType)

        resultAsString(response)
    }
}
