/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.underskrift.fake.FakeUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.nias.NiasUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnderskriftServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "luse";
    private static final String ENHET_ID = "enhet-1";
    private static final String PERSON_ID = "19121212-1212";
    private static final String TICKET_ID = "ticket-1";
    private static final Long VERSION = 1L;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private GrpUnderskriftServiceImpl grpUnderskriftService;

    @Mock
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private FakeUnderskriftService fakeUnderskriftService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LogService logService;

    @Mock
    private IntygService intygService;

    @Mock
    private RedisTicketTracker redisTicketTracker;

    @Mock
    private NiasUnderskriftService niasUnderskriftService;

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
        initVardperson();
        vardenhet = new Vardenhet(ENHET_ID, "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");
        vardgivare.setVardenheter(Collections.singletonList(vardenhet));

        user = createWebCertUser(true);
        when(webCertUserService.getUser()).thenReturn(user);

        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);

        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        when(utlatande.getGrundData()).thenReturn(grunddata);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(moduleApi
                .updateBeforeSigning(anyString(), any(), any(LocalDateTime.class)))
                        .thenReturn("json");

    }

    @Test
    public void testStartSigning() {
        when(utkastRepository.findOne(INTYG_ID))
                .thenReturn(createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", vardperson,
                        ENHET_ID, PERSON_ID));

        when(xmlUnderskriftService.skapaSigneringsBiljettMedDigest(anyString(), anyString(), anyLong(), anyString()))
                .thenReturn(createSignaturBiljett());

        SignaturBiljett sb = testee.startSigningProcess(INTYG_ID, INTYG_TYP, 1L);
        assertNotNull(sb.getIntygSignature());
        assertNotNull(sb.getHash());

        verify(niasUnderskriftService, times(1)).startNiasCollectPoller(anyString(), any(SignaturBiljett.class));
    }

    private SignaturBiljett createSignaturBiljett() {
        return SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                .withTicketId(TICKET_ID)
                .withIntygsId(INTYG_ID)
                .withVersion(VERSION)
                .withStatus(SignaturStatus.BEARBETAR)
                .withSkapad(LocalDateTime.now())
                .withHash("hash")
                .withIntygSignature(new IntygXMLDSignature())
                .build();
    }

    private WebCertUser createWebCertUser(boolean doctor) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setNamn(hoSPerson.getFullstandigtNamn());
        user.setHsaId(hoSPerson.getPersonId());
        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);
        user.setAuthenticationMethod(AuthenticationMethod.EFOS);

        return user;
    }

    private void initVardperson() {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId("AAA");
        hoSPerson.setFullstandigtNamn("Dr Dengroth");

        vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getPersonId());
        vardperson.setNamn(hoSPerson.getFullstandigtNamn());
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model,
            VardpersonReferens vardperson, String enhetsId, String personId) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setEnhetsId(enhetsId);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(personId).get());

        return utkast;
    }
}
