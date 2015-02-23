package se.inera.webcert.service.notification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

@RunWith(MockitoJUnitRunner.class)
public class SendNotificationStrategyTest {

    private static final String INTYG_ID_1 = "intyg-1";
    private static final String INTYG_ID_2 = "intyg-2";
    private static final String INTYG_ID_3 = "intyg-3";

    private static final String INTYG_FK = "fk7263";
    private static final String INTYG_TS = "ts-bas";

    private static final String ENHET_1 = "SE12345678-1000";
    private static final String ENHET_2 = "SE12345678-2000";
    private static final String ENHET_3 = "SE12345678-3000";

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterRegistry;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @InjectMocks
    private SendNotificationStrategy sendStrategy = new DefaultSendNotificationStrategyImpl();

    @Before
    public void setupIntegreradeEnheter() {
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(ENHET_1)).thenReturn(Boolean.TRUE);
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(ENHET_2)).thenReturn(Boolean.FALSE);
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(ENHET_3)).thenReturn(Boolean.TRUE);
    }

    @Before
    public void setupUtkastRepository() {
        when(mockUtkastRepository.exists(INTYG_ID_1)).thenReturn(Boolean.TRUE);
        when(mockUtkastRepository.exists(INTYG_ID_2)).thenReturn(Boolean.TRUE);
        when(mockUtkastRepository.exists(INTYG_ID_3)).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testUtkastOk() {

        boolean res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_FK, ENHET_1));
        assertTrue("Should return true", res);
        
        verify(mockIntegreradeEnheterRegistry).isEnhetIntegrerad(ENHET_1);
    }

    @Test
    public void testUtkastUnitNotIntegrated() {
        boolean res  = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_FK, ENHET_2));
        assertFalse("Should return false, since ENHET_2 is not integrated", res);
        
        verify(mockIntegreradeEnheterRegistry).isEnhetIntegrerad(ENHET_2);
    }

    @Test
    public void testUtkastWrongType() {
        boolean res  = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_TS, ENHET_1));
        assertFalse("Only fk7263 is permitted", res);
        verifyZeroInteractions(mockIntegreradeEnheterRegistry);
    }

    @Test
    public void testWithFragaSvarOk() {

        boolean res = sendStrategy.decideNotificationForFragaSvar(createFragaSvar(INTYG_ID_1, INTYG_FK, ENHET_1));
        assertTrue(res);

        verify(mockUtkastRepository).exists(INTYG_ID_1);
        verify(mockIntegreradeEnheterRegistry).isEnhetIntegrerad(ENHET_1);
    }
    
    @Test
    public void testWithFragaSvarWrongType() {

        boolean res = sendStrategy.decideNotificationForFragaSvar(createFragaSvar(INTYG_ID_1, INTYG_TS, ENHET_1));
        assertFalse(res);
        
        verifyZeroInteractions(mockUtkastRepository);
        verifyZeroInteractions(mockIntegreradeEnheterRegistry);
    }
    
    @Test
    public void testWithFragaSvarUnitNotIntegrated() {

        boolean res = sendStrategy.decideNotificationForFragaSvar(createFragaSvar(INTYG_ID_1, INTYG_FK, ENHET_2));
        assertFalse(res);
        
        verify(mockUtkastRepository).exists(INTYG_ID_1);
        verify(mockIntegreradeEnheterRegistry).isEnhetIntegrerad(ENHET_2);
    }
    
    @Test
    public void testWithFragaSvarUnitIntygNotPresent() {

        boolean res = sendStrategy.decideNotificationForFragaSvar(createFragaSvar(INTYG_ID_3, INTYG_FK, ENHET_1));
        assertFalse(res);
        
        verify(mockUtkastRepository).exists(INTYG_ID_3);
        verifyZeroInteractions(mockIntegreradeEnheterRegistry);
    }

    private FragaSvar createFragaSvar(String intygsId, String intygsTyp, String enhetsId) {

        FragaSvar fs = new FragaSvar();

        IntygsReferens intygsRef = new IntygsReferens();
        intygsRef.setIntygsId(intygsId);
        intygsRef.setIntygsTyp(intygsTyp);

        fs.setIntygsReferens(intygsRef);

        fs.setVardperson(new Vardperson());
        fs.getVardperson().setEnhetsId(enhetsId);

        return fs;
    }

    private Utkast createUtkast(String intygId, String intygsTyp, String enhetsId) {

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId("SE12345678-0000");
        vardperson.setNamn("Dr Börje Dengroth");

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(intygsTyp);
        utkast.setEnhetsId(enhetsId);
        utkast.setEnhetsNamn("Vårdenheten");
        utkast.setPatientPersonnummer("19121212-1212");
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvansson");
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel("{model}");
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }
}
