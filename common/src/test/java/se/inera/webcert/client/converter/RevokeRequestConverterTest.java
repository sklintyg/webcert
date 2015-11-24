package se.inera.intyg.webcert.common.client.converter;

import javax.xml.bind.JAXBException;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import iso.v21090.dt.v1.II;

/**
 * Created by eriklupander on 2015-05-22.
 */
public class RevokeRequestConverterTest {

    RevokeRequestConverterImpl testee = new RevokeRequestConverterImpl();

    @Before
    public void setupJaxb() throws JAXBException {
        testee.initializeJaxbContext();
    }

    @Test
    public void testToXml() throws JAXBException {

        RevokeMedicalCertificateRequestType request = buildRequest();
        String xml = testee.toXml(request);
        Assert.assertNotNull(xml);
    }

    @Test
    public void testFromXml() throws JAXBException {

        // Eating our own dog-food here. Should use stored XML instead..
        RevokeMedicalCertificateRequestType request = buildRequest();
        String xml = testee.toXml(request);
        RevokeMedicalCertificateRequestType rebuilt = testee.fromXml(xml);
        Assert.assertNotNull(rebuilt);
        Assert.assertTrue(request.equals(rebuilt));
    }

    private RevokeMedicalCertificateRequestType buildRequest() {
        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(buildRevokeType());
        return request;
    }

    private RevokeType buildRevokeType() {
        RevokeType revokeType = new RevokeType();
        revokeType.setAdressVard(buildVardAdresseringsType());
        revokeType.setAvsantTidpunkt(LocalDateTime.now().withMillisOfDay(0));
        revokeType.setLakarutlatande(buildLakarUtlatandeEnkel());
        revokeType.setMeddelande("MEDDELANDE");
        revokeType.setVardReferensId("REFERNSID");
        return revokeType;
    }

    private LakarutlatandeEnkelType buildLakarUtlatandeEnkel() {
        LakarutlatandeEnkelType lakarutlatandeEnkelType = new LakarutlatandeEnkelType();
        lakarutlatandeEnkelType.setLakarutlatandeId("ID");
        lakarutlatandeEnkelType.setPatient(buildPatientType());
        lakarutlatandeEnkelType.setSigneringsTidpunkt(LocalDateTime.now().withMillisOfDay(0));
        return lakarutlatandeEnkelType;
    }

    private PatientType buildPatientType() {
        PatientType patientType = new PatientType();
        patientType.setFullstandigtNamn("NAMN NAMNSSON");
        patientType.setPersonId(new II());
        return patientType;
    }

    private VardAdresseringsType buildVardAdresseringsType() {
        VardAdresseringsType vardAdresseringsType = new VardAdresseringsType();
        vardAdresseringsType.setHosPersonal(buildHosPersonalType());

        return vardAdresseringsType;
    }

    private HosPersonalType buildHosPersonalType() {
        HosPersonalType hosPersonalType = new HosPersonalType();
        hosPersonalType.setEnhet(buildEnhetType());
        return hosPersonalType;
    }

    private EnhetType buildEnhetType() {
        EnhetType enhetType = new EnhetType();
        enhetType.setEnhetsnamn("TEST");
        enhetType.setVardgivare(buildVardgivareType());
        return enhetType;
    }

    private VardgivareType buildVardgivareType() {
        VardgivareType vardgivareType = new VardgivareType();
        vardgivareType.setVardgivareId(new II());
        vardgivareType.setVardgivarnamn("NAMN");
        return vardgivareType;
    }
}
