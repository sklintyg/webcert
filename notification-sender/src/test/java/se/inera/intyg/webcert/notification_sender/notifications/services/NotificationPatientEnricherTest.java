package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-08-18.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationPatientEnricherTest {

    @Mock
    private PUService puService;

    @InjectMocks
    private NotificationPatientEnricher testee;

    @Test
    public void testFk7263IsNotEnriched() {
        testee.enrichWithPatient(buildIntyg("fk7263"));
        verifyZeroInteractions(puService);
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionIsThrownWhenPuInvocationFails() {
        when(puService.getPerson(any(Personnummer.class)))
                .thenReturn(new PersonSvar(NotificationTestHelper.buildPerson(false), PersonSvar.Status.ERROR));
        try {
            testee.enrichWithPatient(buildIntyg("lisjp"));
        } catch (Exception e) {
            verify(puService, times(1)).getPerson(any(Personnummer.class));
            throw e;
        }
    }

    @Test
    public void testLuaeFsIsEnriched() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));
        Intyg intyg = buildIntyg("luae_fs");
        testee.enrichWithPatient(intyg);
        verify(puService, times(1)).getPerson(any(Personnummer.class));

        Patient p = intyg.getPatient();
        assertEquals("Tolvan", p.getFornamn());
        assertEquals("Mellis", p.getMellannamn());
        assertEquals("Tolvansson", p.getEfternamn());
        assertEquals("Tolvgatan 12", p.getPostadress());
        assertEquals("12121", p.getPostnummer());
        assertEquals("Tolvhult", p.getPostort());
    }

    @Test
    public void testSekretessmarkeradPatientIsNotUpdated() {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));
        Intyg intyg = buildIntyg("luae_fs");
        testee.enrichWithPatient(intyg);
        verify(puService, times(1)).getPerson(any(Personnummer.class));

        Patient p = intyg.getPatient();
        assertEquals("191212121212", p.getPersonId().getExtension());
        assertNull(p.getFornamn());
        assertNull(p.getMellannamn());
        assertEquals("Sekretessmarkering", p.getEfternamn());
        assertNull(p.getPostadress());
        assertNull(p.getPostnummer());
        assertNull(p.getPostort());
    }

    private Intyg buildIntyg(String intygsTyp) {
        Intyg intyg = new Intyg();
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        intyg.setTyp(typAvIntyg);
        intyg.setPatient(NotificationTestHelper.buildPatient());
        return intyg;
    }


    private PersonSvar buildPersonSvar(boolean sekretessmarkering) {
        return new PersonSvar(NotificationTestHelper.buildPerson(sekretessmarkering), PersonSvar.Status.FOUND);
    }

}
