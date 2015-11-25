package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Override
    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testSendIntyg() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());

        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString());

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))).thenReturn(response);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        // verify(omsandningRepository).save(any(Omsandning.class));
        // verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString());
        // verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class))
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
    // public void testSendIntygFailingWithError() throws Exception {
    //
    // SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
    // response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Error text"));
    //
    // when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
    // .thenReturn(response);
    //
    // // Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
    // // omsandning.setConfiguration(CONFIG_AS_JSON);
    //
    // IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
    // assertEquals(IntygServiceResult.RESCHEDULED, res);
    //
    // // verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    // verify(intygRepository, times(0)).save(any(Utkast.class));
    // }

    // TODO send fail is now handled by certificate-sender, create test there instead.
    // @Test
    // public void testSendIntygSendServiceFailingWithRuntimeException() throws Exception {
    //
    // when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
    // .thenThrow(new RuntimeException("A runtime exception"));
    //
    // Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
    // omsandning.setConfiguration(CONFIG_AS_JSON);
    //
    // try {
    // intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
    // Assert.fail("WebCertServiceException expected");
    // } catch (WebCertServiceException e) {
    // // Expected
    // }
    //
    // verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    // verify(intygRepository, times(0)).save(any(Utkast.class));
    // }

    @Test
    public void testSendIntygPDLLogServiceFailingWithRuntimeException() throws Exception {

        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);

        doThrow(new RuntimeException("")).when(logService).logSendIntygToRecipient(any(LogRequest.class));

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
            Assert.fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }
        verify(intygRepository, times(0)).save(any(Utkast.class));
    }

    private WebCertUser createUser() {
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());
        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
