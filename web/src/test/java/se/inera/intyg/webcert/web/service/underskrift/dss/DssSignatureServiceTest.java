package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.xml.transform.StringResult;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignRequest;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
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
    DssSignMessageService dssSignMessageService;

    @Mock
    WebCertUser user;

    @Mock
    UtkastRepository utkastRepository;

    @Mock
    Optional<Utkast> utkastOptional;

    @Mock
    Utkast utkast;

    @Captor
    ArgumentCaptor<SignRequest> signRequestCaptor;

    @Mock
    Personnummer patient;

    @Mock
    UnderskriftService underskriftService;

    DssSignatureService dssSignatureService;

    static Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    static {
        String[] packages = {"oasis.names.tc", "org.w3._2000._09.xmldsig_", "org.w3._2001._04.xmlenc_", "se.elegnamnden.id.csig"};
        marshaller.setPackagesToScan(packages);

        marshaller.setMarshallerProperties(new HashMap<String, Object>() {
            {
                put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, false);
                put(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
            }
        });
    }


    public DssSignatureServiceTest() {
        MockitoAnnotations.initMocks(this);
        dssSignatureService = new DssSignatureService(dssMetadataService, dssSignMessageService, webCertUserService, utkastRepository,
            underskriftService);
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
        when(dssSignMessageService.signSignRequest(Mockito.any()))
            .thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");

        ReflectionTestUtils.setField(dssSignatureService, "webcertHostUrl", "https://wc.localtest.me:9088");
        ReflectionTestUtils.setField(dssSignatureService, "customerId", "AnUn3ss3c@ary_Long--$@ClientIDwithr4nd0mCharacters");
        ReflectionTestUtils.setField(dssSignatureService, "applicationId", "App/ID\\With--w€|rd__CH@r$");
        ReflectionTestUtils.setField(dssSignatureService, "idpUrl", "https://idpurl.se/samlv2/idp/metadata");
        ReflectionTestUtils.setField(dssSignatureService, "serviceUrl",
            "https://esign.v2.st.signatureservice.se/signservice-frontend/metadata/4321a111111");
        ReflectionTestUtils.setField(dssSignatureService, "signMessage",
            "Härmed skriver jag under {intygsTyp} utfärdat för {patientPnr}<br><br>Intygs-id: {intygsId}");

        var sb = SignaturBiljettBuilder.aSignaturBiljett("ticketId", SignaturTyp.XMLDSIG, SignMethod.SIGN_SERVICE).withIntygsId("intygsId")
            .withHash("HASH").build();

        var createSignatureRequestDTOResponse = dssSignatureService.createSignatureRequestDTO(sb);
        verify(dssSignMessageService).signSignRequest(signRequestCaptor.capture());
        var capturedSignRequest = signRequestCaptor.getValue();

        // TODO Assert some things in the capturedSignRequest

        System.out.println(toXmlString(capturedSignRequest));

        assertNotNull(createSignatureRequestDTOResponse);
    }

    private String toXmlString(SignRequest signRequest) {
        var stringResult = new StringResult();
        marshaller.marshal(signRequest, stringResult);

        return stringResult.toString();
    }

    @Test
    @Ignore
    public void receiveSignatureResponse() throws IOException {
        var stream = new ClassPathResource("dss/signResponse.xml").getInputStream();
        var string = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
        dssSignatureService.receiveSignResponse(string);
    }
}