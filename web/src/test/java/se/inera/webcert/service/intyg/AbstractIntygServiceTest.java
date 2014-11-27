package se.inera.webcert.service.intyg;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManagerImpl;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.web.service.WebCertUserService;

public abstract class AbstractIntygServiceTest {
    
    protected static final String CONFIG_AS_JSON = "{config-as-json}";

    protected static final String INTYG_ID = "intyg-1";
    
    protected static final String INTYG_TYP_FK = "fk7263";

    @Mock
    protected GetCertificateForCareResponderInterface getCertificateService;
    
    @Mock
    protected OmsandningRepository omsandningRepository;

    @Mock
    protected RegisterCertificateResponderInterface intygSender;
    
    @Mock
    protected SendMedicalCertificateResponderInterface sendService;
    
    @Mock
    protected RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    protected IntygModuleFacade moduleFacade;
    
    // Here we test the real converter
    @Spy
    protected IntygServiceConverter serviceConverter = new IntygServiceConverterImpl();
        
    @Mock
    protected WebCertUserService webCertUserService;
    
    @Mock
    protected LogService logService;
    
    // Here we use the real config manager
    @Spy
    protected IntygServiceConfigurationManager configurationManager = new IntygServiceConfigurationManagerImpl(new CustomObjectMapper());
    
    @InjectMocks
    protected IntygServiceImpl intygService = new IntygServiceImpl();
    
    protected String json;
    protected Utlatande utlatande;
    protected CertificateResponse certificateResponse;
    protected CertificateResponse revokedCertificateResponse;
    
    @Before
    public void setupIntygstjanstResponse() throws Exception {

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = new CustomObjectMapper().readValue(json, Utlatande.class);
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        revokedCertificateResponse = new CertificateResponse(json, utlatande, metaData, true);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(certificateResponse);
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString())).thenReturn(true);
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
    
}
