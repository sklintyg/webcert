package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import se.inera.intyg.webcert.web.service.intyg.dto.IntygMetaData;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

public class IntygMetaDataConverterTest {

    @Test
    public void convertNullTest() {
        Optional<IntygMetaData> result = IntygMetaDataConverter.convert(null);
        assertFalse(result.isPresent());
    }

    @Test
    public void convertTest() {
        final String intygId = "intygid";
        final String intygTyp = "intygtyp";
        final String signeratAv = "signeratav";
        final String enhet = "enhet";
        CertificateMetaType certificateMetaType = new CertificateMetaType();
        certificateMetaType.setCertificateId(intygId);
        certificateMetaType.setCertificateType(intygTyp);
        certificateMetaType.setIssuerName(signeratAv);
        certificateMetaType.setFacilityName(enhet);
        Optional<IntygMetaData> result = IntygMetaDataConverter.convert(certificateMetaType);
        assertTrue(result.isPresent());
        assertEquals(intygId, result.get().getIntygId());
        assertEquals(intygTyp, result.get().getIntygTyp());
        assertEquals(signeratAv, result.get().getSigneratAv());
        assertEquals(enhet, result.get().getEnhet());
    }
}
