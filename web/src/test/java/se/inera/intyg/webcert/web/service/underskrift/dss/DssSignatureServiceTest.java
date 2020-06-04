package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett.SignaturBiljettBuilder;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public class DssSignatureServiceTest {

    @Mock
    DssMetadataService dssMetadataService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    WebCertUser user;

    @Mock
    UtkastRepository utkastRepository;

    @Mock
    Optional<Utkast> utkastOptional;

    @Mock
    Utkast utkast;

    @Mock
    Personnummer patient;

    DssSignatureService dssSignatureService;

    public DssSignatureServiceTest() {
        MockitoAnnotations.initMocks(this);
        dssSignatureService = new DssSignatureService(dssMetadataService, webCertUserService, utkastRepository);
    }

    @BeforeClass
    public static void init() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
    }

    @Test
    public void createSignatureRequestDTO() {
        when(dssMetadataService.getDssActionUrl()).thenReturn("ActionUrl");
        when(webCertUserService.getUser()).thenReturn(user);
        when(user.getPersonId()).thenReturn("191212121212");
        when(utkastRepository.findById(Mockito.anyString())).thenReturn(utkastOptional);
        when(utkastOptional.orElse(Mockito.any())).thenReturn(utkast);
        when(utkast.getIntygsTyp()).thenReturn("intygsTyp");
        when(utkast.getPatientPersonnummer()).thenReturn(patient);
        when(patient.getPersonnummerWithDash()).thenReturn("19121212-1212");

        ReflectionTestUtils.setField(dssSignatureService, "webcertHostUrl", "https://wc.localtest.me:9088");
        ReflectionTestUtils.setField(dssSignatureService, "customerId", "clientId");
        ReflectionTestUtils.setField(dssSignatureService, "applicationId", "appId");
        ReflectionTestUtils.setField(dssSignatureService, "idpUrl", "https://idpurl.se/samlv2/idp/metadata");
        ReflectionTestUtils.setField(dssSignatureService, "serviceUrl",
            "https://esign.v2.st.signatureservice.se/signservice-frontend/metadata/4321a111111");
        ReflectionTestUtils.setField(dssSignatureService, "signMessage",
            "Härmed skriver jag under {intygsTyp} utfärdat för {patientPnr}<br><br>Intygs-id: {intygsId}");

        var sb = SignaturBiljettBuilder.aSignaturBiljett("ticketId", SignaturTyp.XMLDSIG, SignMethod.SIGN_SERVICE).withIntygsId("intygsId")
            .withHash("HASH").build();
        var res = dssSignatureService.createSignatureRequestDTO(sb);

//        System.out.println(res.getSignRequest());

        assertNotNull(res);
    }
}