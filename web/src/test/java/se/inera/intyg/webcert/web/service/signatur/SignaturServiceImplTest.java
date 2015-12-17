/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.signatur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.util.ReflectionUtils.setTypedField;

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
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.hsa.model.AuthenticationMethod;
import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
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

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class SignaturServiceImplTest extends AuthoritiesConfigurationTestSetup {

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

        setTypedField(intygSignatureService, new SignaturTicketTracker());
    }

    private WebCertUser createWebCertUser(boolean doctor) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
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

        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);

        user = createWebCertUser(true);
        user.setAuthenticationMethod(AuthenticationMethod.NET_ID);
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setPrivatLakareAvtalGodkand(true);
        user.setPersonId(PERSON_ID);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(completedUtkast)).thenReturn(completedUtkast);
        when(webcertUserService.getUser()).thenReturn(user);
        when(asn1Util.parsePersonId(any(InputStream.class))).thenReturn("other-person-id-1");

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        intygSignatureService.clientSignature(ticket.getId(), "test");
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
