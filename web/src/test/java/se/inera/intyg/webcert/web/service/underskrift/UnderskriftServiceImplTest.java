/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus.SIGNERAD;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.ENHET_ID;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.INTYG_ID;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.INTYG_TYP;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.PERSON_ID;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.TICKET_ID;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createSignaturBiljett;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createUtkast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.underskrift.fake.FakeUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UnderskriftServiceImplTest extends AuthoritiesConfigurationTestSetup {

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private GrpUnderskriftServiceImpl grpUnderskriftService;

    @Mock
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private FakeUnderskriftService fakeUnderskriftService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LogService logService;

    @Mock
    private LogRequestFactory logRequestFactory;

    @Mock
    private IntygService intygService;

    @Mock
    private RedisTicketTracker redisTicketTracker;

    @Mock
    private DraftAccessService draftAccessService;

    @Mock
    private AccessResultExceptionHelper accessResultExceptionHelper;

    @InjectMocks
    private UnderskriftServiceImpl testee;

    private HoSPersonal hoSPerson;
    private VardpersonReferens vardperson;
    private Vardenhet vardenhet;
    private Vardgivare vardgivare;
    private WebCertUser user;
    private ModuleApi moduleApi = mock(ModuleApi.class);

    @Before
    public void init() throws IOException, ModuleNotFoundException, ModuleException {
        vardperson = UnderskriftTestUtil.createVardperson();
        vardenhet = new Vardenhet(ENHET_ID, "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");
        vardgivare.setVardenheter(Collections.singletonList(vardenhet));

        user = createWebCertUser(true);
        when(webCertUserService.getUser()).thenReturn(user);

        when(moduleRegistry.getModuleApi(or(isNull(), anyString()), or(isNull(), anyString()))).thenReturn(moduleApi);

        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        when(utlatande.getGrundData()).thenReturn(grunddata);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(moduleApi
            .updateBeforeSigning(anyString(), any(), any(LocalDateTime.class)))
            .thenReturn("json");

        when(utkastService.checkIfPersonHasExistingIntyg(any(), any())).thenReturn(ImmutableMap.of(
            "utkast", ImmutableMap.of(),
            "intyg", ImmutableMap.of()));

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class))).thenReturn(new LogRequest());
    }

    @Test
    public void testStartSigning() {
        when(utkastRepository.findById(INTYG_ID))
            .thenReturn(Optional.of(createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
                ENHET_ID, PERSON_ID)));

        when(xmlUnderskriftService.skapaSigneringsBiljettMedDigest(anyString(), anyString(), anyLong(), anyString(), any(SignMethod.class),
            anyString()))
            .thenReturn(createSignaturBiljett(SignaturStatus.BEARBETAR));

        SignaturBiljett sb = testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.NETID_PLUGIN, TICKET_ID);
        assertNotNull(sb.getIntygSignature());
        assertNotNull(sb.getHash());
    }

    @Test(expected = WebCertServiceException.class)
    public void testStartSignNoUtkastFound() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());
        testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.FAKE, TICKET_ID);
    }

    @Test(expected = OptimisticLockException.class)
    public void testStartSignUtkastVersionsDifferFound() {
        when(utkastRepository.findById(INTYG_ID))
            .thenReturn(Optional.of(createUtkast(INTYG_ID, 2L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
                ENHET_ID, PERSON_ID)));
        testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.FAKE, TICKET_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testStartSignUtkastNotReadyForSign() {
        when(utkastRepository.findById(INTYG_ID))
            .thenReturn(Optional.of(createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_INCOMPLETE, "model", vardperson,
                ENHET_ID, PERSON_ID)));
        testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.FAKE, TICKET_ID);
    }

    @Test
    public void testStartSignUtkastWithUndantagUnikOmSenaste() {
        final LocalDateTime ogIntygSkapad = LocalDateTime.of(2018, 5, 5, 5, 5);
        final LocalDateTime ersattandeIntygSkapad = ogIntygSkapad.plusDays(1);
        final String doiTyp = "doi";

        when(utkastRepository.findById(INTYG_ID))
            .thenReturn(Optional.of(createUtkast(INTYG_ID, 1L, doiTyp, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
                ENHET_ID, PERSON_ID, ersattandeIntygSkapad)));

        when(xmlUnderskriftService.skapaSigneringsBiljettMedDigest(anyString(), anyString(), anyLong(), anyString(), any(SignMethod.class),
            anyString()))
            .thenReturn(createSignaturBiljett(SignaturStatus.BEARBETAR));

        when(utkastService.checkIfPersonHasExistingIntyg(any(), any())).thenReturn(ImmutableMap.of(
            "utkast", ImmutableMap.of(),
            "intyg", ImmutableMap.of(doiTyp, PreviousIntyg.of(true, true, "name", "id", ogIntygSkapad))));

        Feature feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_UNIKT_UNDANTAG_OM_SENASTE_INTYG);
        feature.setIntygstyper(ImmutableList.of(DoiModuleEntryPoint.MODULE_ID));
        feature.setGlobal(true);

        user.setFeatures(ImmutableMap.of(feature.getName(), feature));

        testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.FAKE, TICKET_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testStartSignUtkastAlreadySigned() {
        when(utkastRepository.findById(INTYG_ID))
            .thenReturn(Optional.of(createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.SIGNED, "model", vardperson,
                ENHET_ID, PERSON_ID)));
        testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L, SignMethod.FAKE, TICKET_ID);
    }

    @Test
    public void testGetSigneringsStatus() {
        when(redisTicketTracker.findBiljett(TICKET_ID)).thenReturn(createSignaturBiljett(SignaturStatus.BEARBETAR));

        SignaturBiljett signaturBiljett = testee.signeringsStatus(TICKET_ID);
        assertNotNull(signaturBiljett);
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetSigneringsStatusNoTicketFound() {
        when(redisTicketTracker.findBiljett(TICKET_ID)).thenReturn(null);

        SignaturBiljett signaturBiljett = testee.signeringsStatus(TICKET_ID);
        assertNotNull(signaturBiljett);
    }

    @Test
    public void testNetidSignature() {
        Utkast utkast = createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
            ENHET_ID, PERSON_ID);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));
        SignaturBiljett signaturBiljett = createSignaturBiljett(SignaturStatus.BEARBETAR);
        when(redisTicketTracker.findBiljett(TICKET_ID)).thenReturn(signaturBiljett);
        when(xmlUnderskriftService.finalizeSignature(any(SignaturBiljett.class), any(byte[].class), anyString(), any(Utkast.class),
            any(WebCertUser.class)))
            .thenReturn(createSignaturBiljett(SIGNERAD));

        SignaturBiljett sb = testee.netidSignature(TICKET_ID, "signatur".getBytes(StandardCharsets.UTF_8), "certifikat");
        assertNotNull(sb);
        assertEquals(SIGNERAD, sb.getStatus());
    }

    @Test
    public void testGrpSignature() {
        Utkast utkast = createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
            ENHET_ID, PERSON_ID);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));
        SignaturBiljett signaturBiljett = createSignaturBiljett(SignaturStatus.BEARBETAR);
        when(redisTicketTracker.findBiljett(TICKET_ID)).thenReturn(signaturBiljett);
        when(grpUnderskriftService.finalizeSignature(any(SignaturBiljett.class), any(byte[].class), ArgumentMatchers.isNull(),
            any(Utkast.class), any(WebCertUser.class)))
            .thenReturn(createSignaturBiljett(SIGNERAD));

        SignaturBiljett sb = testee.grpSignature(TICKET_ID, "signatur".getBytes(StandardCharsets.UTF_8));
        assertNotNull(sb);
        assertEquals(SIGNERAD, sb.getStatus());
    }

    private WebCertUser createWebCertUser(boolean doctor) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        HoSPersonal hoSPerson = UnderskriftTestUtil.createHoSPerson();

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setNamn(hoSPerson.getFullstandigtNamn());
        user.setHsaId(hoSPerson.getPersonId());
        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);
        user.setAuthenticationMethod(AuthenticationMethod.SITHS);
        user.setPersonId(hoSPerson.getPersonId());

        return user;
    }

}
