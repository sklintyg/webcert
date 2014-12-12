package se.inera.webcert.service.intyg;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.draft.IntygSignatureServiceImpl;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManagerImpl;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

public abstract class AbstractIntygServiceTest {

    protected static final String CONFIG_AS_JSON = "{config-as-json}";

    protected static final String INTYG_ID = "intyg-1";
    protected static final String INTYG_TYP_FK = "fk7263";

    protected static final String INTYG_INTERNAL_JSON_MODEL = "{internal-model-as-json}";
    protected static final String INTYG_EXTERNAL_JSON_MODEL = "{external-model-as-json}";


    // -- Just plain mock objects

    @Mock
    protected GetCertificateForCareResponderInterface getCertificateService;

    @Mock
    protected IntygModuleFacade moduleFacade;

    @Mock
    protected IntygRepository intygRepository;

    @Mock
    protected ListCertificatesForCareResponderInterface listCertificateService;

    @Mock
    protected LogService logService;

    @Mock
    protected NotificationService notificationService;

    @Mock
    protected OmsandningRepository omsandningRepository;

    @Mock
    protected RegisterCertificateResponderInterface registerService;

    @Mock
    protected RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    protected SendMedicalCertificateResponderInterface sendService;

    @Mock
    protected WebCertUserService webcertUserService;


    // -- Spy objects --

    // Here we test the real converter
    @Spy
    protected IntygServiceConverter serviceConverter = new IntygServiceConverterImpl();

    // Here we use the real config manager
    @Spy
    protected IntygServiceConfigurationManager configurationManager = new IntygServiceConfigurationManagerImpl(new CustomObjectMapper());


    // -- Injected mock objects --

    @InjectMocks
    protected IntygSignatureServiceImpl intygSignatureService = new IntygSignatureServiceImpl();

    @InjectMocks
    protected IntygServiceImpl intygService = new IntygServiceImpl();


    protected JAXBContext jaxbContext;


    // -- Setup and teardown --

    @Before
    public void setupJaxb() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webcertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(false);
    }

    @Before
    public void setupOmsandningSave() {
        when(omsandningRepository.save(any(Omsandning.class))).thenAnswer(new Answer<Omsandning>() {
            @Override
            public Omsandning answer(InvocationOnMock invocation) throws Throwable {
                return (Omsandning) invocation.getArguments()[0];
            }
        });
    }


    // -- Creator methods --

    protected GetCertificateForCareResponseType makeIntygstjanstResponse() throws JAXBException, IOException {

        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-get-certificate.xml");

        return jaxbContext.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();
    }

    protected Utlatande makeUtlatande() throws Exception {
        return new CustomObjectMapper().readValue(
                new ClassPathResource("IntygServiceTest/utlatande.json").getFile(), MinimalUtlatande.class);
    }


}
