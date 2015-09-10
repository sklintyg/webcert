package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.webcert.common.security.authority.SimpleGrantedAuthority;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.user.dto.WebCertUser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//import se.inera.webcert.persistence.utkast.model.Omsandning;
//import se.inera.webcert.persistence.utkast.model.OmsandningOperation;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testSendIntyg() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());

        WebCertUser webCertUser = new WebCertUser(getGrantedRole(), getGrantedPrivileges());

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

       // verify(omsandningRepository).save(any(Omsandning.class));
       // verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), anyString(), anyString());
        //verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        WebCertUser webCertUser = new WebCertUser(getGrantedRole(), getGrantedPrivileges());

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

       // verify(omsandningRepository).save(any(Omsandning.class));
      //  verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), anyString(), anyString());
//        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))
        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    private Utkast getUtkast(String intygId) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygId);
        return utkast;
    }

    // TODO send fail is now handled by certificate-sender, create test there instead.
    // @Test
//    public void testSendIntygFailingWithError() throws Exception {
//
//        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
//        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Error text"));
//
//        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
//        .thenReturn(response);
//
//       // Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
//      //  omsandning.setConfiguration(CONFIG_AS_JSON);
//
//        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
//        assertEquals(IntygServiceResult.RESCHEDULED, res);
//
//       // verify(omsandningRepository, times(2)).save(any(Omsandning.class));
//        verify(intygRepository, times(0)).save(any(Utkast.class));
//    }

    // TODO send fail is now handled by certificate-sender, create test there instead.
    // @Test
//    public void testSendIntygSendServiceFailingWithRuntimeException() throws Exception {
//
//        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
//                .thenThrow(new RuntimeException("A runtime exception"));
//
//        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
//        omsandning.setConfiguration(CONFIG_AS_JSON);
//
//        try {
//            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
//            Assert.fail("WebCertServiceException expected");
//        } catch (WebCertServiceException e) {
//            // Expected
//        }
//
//        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
//        verify(intygRepository, times(0)).save(any(Utkast.class));
//    }
    
    @Test
    public void testSendIntygPDLLogServiceFailingWithRuntimeException() throws Exception {
                
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);

        doThrow(new RuntimeException("")).when(logService).logSendIntygToRecipient(any(LogRequest.class));
        
       // Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
       // omsandning.setConfiguration(CONFIG_AS_JSON);

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
            Assert.fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }
        verify(intygRepository, times(0)).save(any(Utkast.class));
    }

    private GrantedAuthority getGrantedRole() {
        return new SimpleGrantedAuthority(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE.toString());
    }

    private Collection<? extends GrantedAuthority> getGrantedPrivileges() {
        Set<SimpleGrantedAuthority> privileges = new HashSet<SimpleGrantedAuthority>();
        for (UserPrivilege userPrivilege : UserPrivilege.values()) {
            privileges.add(new SimpleGrantedAuthority(userPrivilege.name(), userPrivilege.toString()));
        }
        return privileges;
    }


}
