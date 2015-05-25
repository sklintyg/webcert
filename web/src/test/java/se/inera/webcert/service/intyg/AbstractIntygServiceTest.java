package se.inera.webcert.service.intyg;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepository;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManager;
import se.inera.webcert.service.intyg.config.IntygServiceConfigurationManagerImpl;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.signatur.SignaturServiceImpl;
import se.inera.webcert.web.service.WebCertUserService;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;


public abstract class AbstractIntygServiceTest {

    protected static final String CONFIG_AS_JSON = "{config-as-json}";

    protected static final String INTYG_ID = "intyg-1";

    protected static final String INTYG_TYP_FK = "fk7263";

    @Mock
    protected RegisterCertificateResponderInterface intygSender;

    @Mock
    protected SendCertificateToRecipientResponderInterface sendService;

    @Mock
    protected RevokeMedicalCertificateResponderInterface revokeService;

    @Mock
    protected IntygModuleFacade moduleFacade;

    @Mock
    protected UtkastRepository intygRepository;

    @Mock
    protected OmsandningRepository omsandningRepository;

    @Mock
    protected WebCertUserService webCertUserService;

    @Mock
    protected LogService logService;

    @Mock
    protected NotificationService notificationService;
    
    @Mock
    protected MonitoringLogService monitoringService;

    // Here we test the real converter
    @Spy
    protected IntygServiceConverter serviceConverter = new IntygServiceConverterImpl();

    // Here we use the real config manager
    @Spy
    protected IntygServiceConfigurationManager configurationManager = new IntygServiceConfigurationManagerImpl(new CustomObjectMapper());

    @InjectMocks
    protected SignaturServiceImpl intygSignatureService = new SignaturServiceImpl();

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
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(false);
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
