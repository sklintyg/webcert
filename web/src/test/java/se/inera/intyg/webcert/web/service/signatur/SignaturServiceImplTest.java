package se.inera.intyg.webcert.web.service.signatur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Util;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.util.ReflectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class SignaturServiceImplTest {

    private static final String ENHET_ID = "testID";

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";
    private static final String PERSON_ID = "191212121212";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private IntygService intygService;

    @Mock
    private LogService logService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private ASN1Util asn1Util;

    @InjectMocks
    private SignaturServiceImpl intygSignatureService = new SignaturServiceImpl();

    private Utkast utkast;

    private Utkast completedUtkast;

    private Utkast signedUtkast;

    private HoSPerson hoSPerson;

    private Vardenhet vardenhet;

    private Vardgivare vardgivare;

    private WebCertUser user;

    @Before
    public void setup() throws ModuleException, ModuleNotFoundException {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        utkast = createUtkast(INTYG_ID, 1, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson, ENHET_ID);
        completedUtkast = createUtkast(INTYG_ID, 2, INTYG_TYPE, UtkastStatus.DRAFT_COMPLETE, INTYG_JSON, vardperson, ENHET_ID);
        signedUtkast = createUtkast(INTYG_ID, 3, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson, ENHET_ID);

        InternalModelResponse internalModelResponse = new InternalModelResponse(INTYG_JSON);
        vardenhet = new Vardenhet(ENHET_ID, "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");
        vardgivare.setVardenheter(Collections.singletonList(vardenhet));

        user = createWebCertUser(true);

        when(webcertUserService.getUser()).thenReturn(user);
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSigning(any(InternalModelHolder.class), any(HoSPersonal.class), any(LocalDateTime.class))).thenReturn(internalModelResponse);

        ReflectionUtils.setTypedField(intygSignatureService, new SignaturTicketTracker());
    }

    private WebCertUser createWebCertUser(boolean doctor) {
        WebCertUser user = new WebCertUser();

        user.setRoles(getGrantedRole(doctor));
        user.setAuthorities(getGrantedPrivileges(doctor));

        user.setNamn(hoSPerson.getNamn());
        user.setHsaId(hoSPerson.getHsaId());
        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);

        return user;
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        intygSignatureService.createDraftHash(INTYG_ID, utkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygAlreadySigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion());
    }

    @Test(expected = OptimisticLockException.class)
    public void getSignatureHashReturnsErrorIfWrongVersion() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion() - 1);
    }

    @Test
    public void getSignatureHashReturnsTicket() throws ModuleNotFoundException, ModuleException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(completedUtkast.getVersion(), ticket.getVersion());
        assertEquals(SignaturTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        intygSignatureService.clientSignature("unknownId", "SIGNATURE");
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        completedUtkast.setModel("{}");

        String signature = "{\"signatur\":\"SIGNATURE\"}";

        intygSignatureService.clientSignature(ticket.getId(), signature);
    }

    @Test
    public void clientSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), any(LogUser.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void clientGrpSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientGrpSignature(ticket.getId(), signature, user);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), any(LogUser.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void serverSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());
    }
    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedDraft() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorDraft() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedClientSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorClientSignature() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedServerSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare> emptyList());

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorServerSignature() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void abortClientSignIfHsaIdOnSigDoesNotMatchSession() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        user = createWebCertUser(true);
        user.setAuthenticationMethod(AuthenticationMethod.SITHS);
        when(webcertUserService.getUser()).thenReturn(user);
        when(asn1Util.parseHsaId(any(InputStream.class))).thenReturn("other-hsa-1");

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void abortClientSignIfPersonIdOnSigDoesNotMatchSession() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);

        user = createWebCertUser(true);
        user.setAuthenticationMethod(AuthenticationMethod.NET_ID);
        user.setRoles(getGrantedRoleForPrivatlakare());
        user.setPrivatLakareAvtalGodkand(true);
        user.setPersonId(PERSON_ID);
        when(webcertUserService.getUser()).thenReturn(user);
        when(asn1Util.parsePersonId(any(InputStream.class))).thenReturn("other-person-id-1");

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        intygSignatureService.clientSignature(ticket.getId(), "test");
    }



    private Map<String, UserRole> getGrantedRoleForPrivatlakare() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_PRIVATLAKARE.name(), UserRole.ROLE_PRIVATLAKARE);
        return map;
    }

    private Map<String, UserRole> getGrantedRole(boolean doctor) {
        Map<String, UserRole> map = new HashMap<>();

        if (doctor) {
            map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        } else {
            map.put(UserRole.ROLE_VARDADMINISTRATOR.name(), UserRole.ROLE_VARDADMINISTRATOR);
        }

        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges(boolean doctor) {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());
        Map<String, UserPrivilege> privilegeMap = new HashMap<>();

        // convert list to map
        if (doctor) {
            privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
                @Override
                public String apply(UserPrivilege userPrivilege) {
                    return userPrivilege.name();
                }
            });
        }

        return privilegeMap;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model, VardpersonReferens vardperson, String enhetsId) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setEnhetsId(enhetsId);
        return utkast;
    }

}
