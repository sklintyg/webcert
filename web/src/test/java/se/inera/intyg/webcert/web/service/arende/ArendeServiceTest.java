package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygMetaData;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest {

    private static final long FIXED_TIME_MILLIS = 1456329300599L;

    @Mock
    private ArendeRepository repo;

    @Mock
    private IntygService intygService;

    @Mock
    private WebCertUserService webcertUserService;

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
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String enhet = "enhet";
        Arende arende = new Arende();
        when(repo.save(any(Arende.class))).thenReturn(arende);
        IntygMetaData intygMetaData = new IntygMetaData();
        intygMetaData.setIntygTyp(intygTyp);
        intygMetaData.setSigneratAv(signeratAv);
        intygMetaData.setEnhet(enhet);
        when(intygService.fetchIntygMetaData(anyString())).thenReturn(intygMetaData);
        Arende res = service.processIncomingMessage(arende);
        assertNotNull(res);
        assertEquals(FIXED_TIME_MILLIS, res.getTimestamp().toDateTime().getMillis());
        assertEquals(Status.PENDING_INTERNAL_ACTION, res.getStatus());
        assertEquals(intygTyp, res.getIntygTyp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(enhet, res.getEnhet());
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
}
