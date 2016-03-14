package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.converter.util.TransportToArende;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final long FIXED_TIME_MILLIS = 1456329300599L;
    private static final LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = new LocalDateTime("9999-12-11T10:22:00");
    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");

    @Mock
    private ArendeRepository repo;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private MonitoringLogService monitoringLog;

    @Mock
    private TransportToArende transportToArende;

    @InjectMocks
    private ArendeServiceImpl service;

    @Before
    public void setUp() {
        DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_MILLIS);
    }

    @After
    public void cleanUp() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testProcessIncomingMessage() throws WebCertServiceException {
        final String intygId = "intygsid";
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String enhet = "enhet";
        Arende arende = new Arende();
        arende.setIntygsId(intygId);
        when(repo.save(any(Arende.class))).thenReturn(arende);
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygTyp);
        utkast.setEnhetsId(enhet);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(utkastRepository.findOne(intygId)).thenReturn(utkast);
        Arende res = service.processIncomingMessage(arende);
        assertNotNull(res);
        assertEquals(FIXED_TIME_MILLIS, res.getTimestamp().toDateTime().getMillis());
        assertEquals(Status.PENDING_INTERNAL_ACTION, res.getStatus());
        assertEquals(intygTyp, res.getIntygTyp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(enhet, res.getEnhet());

        verify(utkastRepository).findOne(intygId);
    }

    @Test
    public void testProcessIncomingMessageCertificateNotFound() {
        when(utkastRepository.findOne(anyString())).thenReturn(null);
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testProcessIncomingMessageCertificateNotSigned() {
        when(utkastRepository.findOne(anyString())).thenReturn(new Utkast());
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
        }
    }

    @Test
    public void testListSignedByForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final List<String> repoResult = Arrays.asList("namn1", "namn2", "namn3");
        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(repo.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);
        List<String> res = service.listSignedByForUnits();
        assertEquals(repoResult, res);
    }

    @Test
    public void testListArendeForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final List<Arende> repoResult = Arrays.asList(new Arende());
        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(repo.findByEnhet(selectedUnits)).thenReturn(repoResult);
        List<Arende> res = service.listArendeForUnits();
        assertEquals(repoResult, res);
    }

    @Test
    public void testGetArendeForIntyg() {
        List<Arende> arendeList = new ArrayList<>();
        arendeList.add(buildArende(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        arendeList.add(buildArende(2L, new LocalDateTime(), new LocalDateTime()));
        arendeList.add(buildArende(3L, JANUARY, JANUARY));

        when(repo.findByIntygsId("intyg-1")).thenReturn(new ArrayList<>(arendeList));
        when(transportToArende.decorate(arendeList.get(0))).thenReturn(arendeList.get(0));
        when(transportToArende.decorate(arendeList.get(1))).thenReturn(arendeList.get(1));
        when(transportToArende.decorate(arendeList.get(2))).thenReturn(arendeList.get(2));

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> result = service.getArende("intyg-1");

        verify(repo).findByIntygsId("intyg-1");
        verify(webcertUserService).getUser();

        assertEquals(3, result.size());
        assertEquals(arendeList, result);
    }

    private Arende buildArende(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setReferensId("<fk-extern-referens>");
        arende.setId(id);
        arende.setEnhet("enhet");
        arende.setSkickatTidpunkt(fragaSkickadDatum);
        arende.setMeddelande("frageText");
        arende.setTimestamp(svarSkickadDatum);
        List<MedicinsktArende> hej = new ArrayList<MedicinsktArende>();
        arende.setIntygsId("<intygsId>");
        arende.setPatientPersonId(PATIENT_ID.getPersonnummer());
        arende.setKomplettering(hej);
        return arende;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }
}
