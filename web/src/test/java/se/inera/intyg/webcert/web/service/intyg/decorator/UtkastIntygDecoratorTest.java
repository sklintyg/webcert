package se.inera.intyg.webcert.web.service.intyg.decorator;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-06-23.
 */
@RunWith(MockitoJUnitRunner.class)
public class UtkastIntygDecoratorTest {

    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final String INTYG_ID = "123";

    private Utkast signedUtkast;

    @Mock
    private UtkastRepository utkastRepository;

    @InjectMocks
    private UtkastIntygDecoratorImpl testee;

    @Before
    public void setup() {
        HoSPerson person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
    }

    @Test
    public void testNotAWebcertIntygDoesNotAddAnyStatuses() {
        when(utkastRepository.findOne(anyString())).thenReturn(null);

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);
        assertEquals(1, response.getMetaData().getStatus().size());
    }

    @Test
    public void testRevokedStatusOnIntygDoesNotAddAnyStatuses() {

        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.CANCELLED, "FK", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(2, response.getMetaData().getStatus().size());
    }

    @Test
    public void testRevokedIntygDoesNotAddAnyStatuses() {

        CertificateResponse response = buildCertificateResponse();
        CertificateResponse revokedResponse = new CertificateResponse(response.getInternalModel(), response.getUtlatande(), response.getMetaData(),
                true);

        testee.decorateWithUtkastStatus(revokedResponse);
        assertEquals(1, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentIntygDoesNotAddAnySentStatus() {

        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(2, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentIntygWithRevokedUtkastDoesAddsRevokedStatus() {
        signedUtkast.setSkickadTillMottagareDatum(LocalDateTime.now());
        signedUtkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findOne(anyString())).thenReturn(signedUtkast);
        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(3, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentStatusIsAddedFromUtkast() {
        signedUtkast.setSkickadTillMottagareDatum(LocalDateTime.now());
        when(utkastRepository.findOne(anyString())).thenReturn(signedUtkast);

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);

        assertEquals(2, response.getMetaData().getStatus().size());
        assertEquals(CertificateState.RECEIVED, response.getMetaData().getStatus().get(0).getType());
        assertEquals(CertificateState.SENT, response.getMetaData().getStatus().get(1).getType());
    }

    @Test
    public void testRevokedStatusIsAddedFromUtkast() {
        signedUtkast.setSkickadTillMottagareDatum(LocalDateTime.now());
        signedUtkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findOne(anyString())).thenReturn(signedUtkast);

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);

        assertEquals(3, response.getMetaData().getStatus().size());
        assertEquals(CertificateState.RECEIVED, response.getMetaData().getStatus().get(0).getType());
        assertEquals(CertificateState.SENT, response.getMetaData().getStatus().get(1).getType());
        assertEquals(CertificateState.CANCELLED, response.getMetaData().getStatus().get(2).getType());
    }

    private CertificateResponse buildCertificateResponse() {
        CertificateResponse response = new CertificateResponse("{}", null, buildCertificateMetaData(), false);
        return response;
    }

    // TODO copied from AbstractIntygServiceTest, refactor to avoid duplication
    private CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "FK", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        return metaData;
    }

    private HoSPerson buildHosPerson() {
        HoSPerson person = new HoSPerson();
        person.setHsaId("AAA");
        person.setNamn("Dr Dengroth");
        return person;
    }

    private Utkast buildUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return intyg;
    }

    private VardpersonReferens buildVardpersonReferens(HoSPerson person) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(person.getHsaId());
        vardperson.setNamn(person.getNamn());
        return vardperson;
    }

}
