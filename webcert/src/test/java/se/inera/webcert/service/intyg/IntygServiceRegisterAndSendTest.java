package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ConnectException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepository;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith( MockitoJUnitRunner.class )
public class IntygServiceRegisterAndSendTest {

    private static final String INTYG_ID = "intyg-1";
    
    private static final String INTYG_TYP = "fk7263";

    private static final String INTYG_INTERNAL_JSON_MODEL = "json";
    
    @Mock
    private GetCertificateForCareResponderInterface getCertificateService;
    
    @Mock
    private OmsandningRepository omsandningRepository;

    @Mock
    private RegisterCertificateResponderInterface intygSender;
    
    @Mock
    private SendMedicalCertificateResponderInterface sendService;

    @Mock
    private IntygModuleFacade moduleFacade;
    
    @Mock
    private IntygServiceConverter serviceConverter;
        
    @Mock
    private WebCertUserService webCertUserService;
    
    @InjectMocks
    private IntygServiceImpl intygService = new IntygServiceImpl();
    
    private JAXBContext jaxbContext;
    
    @Before
    public void setup() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
    }
    
    @Test
    public void testStoreIntyg() throws IntygModuleFacadeException {
        
        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);
        
        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.OK);
        when(intygSender.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);
        
        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);
        
        Omsandning omsandning = new Omsandning(OmsandningOperation.STORE_INTYG, INTYG_ID);
                
        boolean res = intygService.storeIntyg(intyg, omsandning);
        assertTrue(res);
        
        // if all went well the resend should be deleted
        verify(omsandningRepository).delete(omsandning);
    }
    
    @Test
    public void testStoreIntygFailingWithTechnicalError() throws IntygModuleFacadeException {
        
        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);
        
        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.ERROR);
        result.setResultText("Error occured!");
        result.setErrorId(ErrorIdType.TECHNICAL_ERROR);
        when(intygSender.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);
        
        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);
        
        Omsandning omsandning = new Omsandning(OmsandningOperation.STORE_INTYG, INTYG_ID);
                
        boolean res = intygService.storeIntyg(intyg, omsandning);
        assertFalse(res);
        
        // this error should schedule a resend
        verify(omsandningRepository).save(omsandning);
    }
    
    @Test
    public void testStoreIntygWithInfoResponse() throws IntygModuleFacadeException {
        
        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);
        
        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.INFO);
        result.setResultText("This is something important!");
        when(intygSender.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);
        
        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);
        
        Omsandning omsandning = new Omsandning(OmsandningOperation.STORE_INTYG, INTYG_ID);
                
        boolean res = intygService.storeIntyg(intyg, omsandning);
        assertTrue(res);
        
        // this should not schedule a resend
        verify(omsandningRepository).delete(omsandning);
    }
    
    @Test
    public void testStoreIntygFailingWithException() throws IntygModuleFacadeException {
        
        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);
        
        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.ERROR);
        result.setResultText("Error occured!");
        result.setErrorId(ErrorIdType.TECHNICAL_ERROR);
        when(intygSender.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenThrow(new WebServiceException());
        
        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);
        
        Omsandning omsandning = new Omsandning(OmsandningOperation.STORE_INTYG, INTYG_ID);
                
        boolean res = intygService.storeIntyg(intyg, omsandning);
        assertFalse(res);
        
        // this error should schedule a resend
        verify(omsandningRepository).save(omsandning);
    }
    
    @Test
    public void testSendIntyg() throws Exception {
        
        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);
        
        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP), any(UtlatandeType.class))).thenReturn(unmarshallResponse);
        
        // simulate conversion
        SendType sendType = new SendType();
        when(serviceConverter.buildSendTypeFromUtlatande(any(Utlatande.class))).thenReturn(sendType);
        
        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);
        response.setResult(result);
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenReturn(response);
                
        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID);
        omsandning.setConfiguration("FK");
        
        boolean res = intygService.sendIntyg(INTYG_ID, omsandning);
        assertTrue(res);
        
        verify(omsandningRepository).delete(omsandning);
    }
    
    @Test
    public void testSendIntygFailingWithErrorResponse() throws Exception {
        
        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);
        
        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP), any(UtlatandeType.class))).thenReturn(unmarshallResponse);
        
        // simulate conversion
        SendType sendType = new SendType();
        when(serviceConverter.buildSendTypeFromUtlatande(any(Utlatande.class))).thenReturn(sendType);
        
        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.ERROR);
        result.setErrorId(ErrorIdEnum.TECHNICAL_ERROR);
        result.setErrorText("A technical error occured");
        response.setResult(result);
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenReturn(response);
                
        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID);
        omsandning.setConfiguration("FK");
        
        boolean res = intygService.sendIntyg(INTYG_ID, omsandning);
        assertFalse(res);
        
        verify(omsandningRepository).save(omsandning);
    }
            
    @Test
    public void testSendIntygFailingWithException() throws Exception {
        
        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);
        
        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP), any(UtlatandeType.class))).thenReturn(unmarshallResponse);
        
        // simulate conversion
        SendType sendType = new SendType();
        when(serviceConverter.buildSendTypeFromUtlatande(any(Utlatande.class))).thenReturn(sendType);
        
        WebServiceException wse = new WebServiceException(new ConnectException("Could not connect..."));
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenThrow(wse);
        
        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, "intyg-2");
        omsandning.setConfiguration("FK");
        
        boolean res = intygService.sendIntyg(INTYG_ID, omsandning);
        assertFalse(res);
        
        verify(omsandningRepository).save(omsandning);
    }
    
    private Utlatande makeUtlatande() throws Exception {
        return new CustomObjectMapper().readValue(
                new ClassPathResource("IntygServiceTest/utlatande.json").getFile(), MinimalUtlatande.class);
    }
    
    private GetCertificateForCareResponseType makeIntygstjanstResponse() throws JAXBException, IOException {
        
        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-get-certificate.xml");
        
        return jaxbContext.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();
    }
}
