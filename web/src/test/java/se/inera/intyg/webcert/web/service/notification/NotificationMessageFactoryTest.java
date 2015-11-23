package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationMessageFactoryTest {

    private static final String INTYGS_ID = "1234";

    @Mock
    private FragorOchSvarCreator mockFragorOchSvarCreator;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @InjectMocks
    private NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactoryImpl();

    @Test
    public void testCreateNotificationMessageForUtkast() {

        Utkast utkast = createUtkast(INTYGS_ID);
        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(utkast, HandelseType.INTYGSUTKAST_SIGNERAT);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelseType.INTYGSUTKAST_SIGNERAT, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals(INTYGS_ID, msg.getIntygsId());
        assertEquals("fk7263", msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());
        assertNotNull(msg.getFragaSvar());
    }

    @Test
    public void testCreateNotificationMessageForFragaSvar() {

        Utkast utkast = createUtkast(INTYGS_ID);
        when(mockUtkastRepository.findOne(INTYGS_ID)).thenReturn(utkast);

        when(mockFragorOchSvarCreator.createFragorOchSvar(INTYGS_ID)).thenReturn(new FragorOchSvar(1, 0, 0, 0));

        FragaSvar fs = createFragaSvar();
        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(fs, HandelseType.FRAGA_TILL_FK);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelseType.FRAGA_TILL_FK, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals("1234", msg.getIntygsId());
        assertEquals("fk7263", msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());

        assertNotNull(msg.getFragaSvar());
        assertEquals(1, msg.getFragaSvar().getAntalFragor());
    }

    private FragaSvar createFragaSvar() {
        FragaSvar fs = new FragaSvar();

        IntygsReferens intygsRef = new IntygsReferens();
        intygsRef.setIntygsId(INTYGS_ID);
        intygsRef.setIntygsTyp("fk7263");

        fs.setIntygsReferens(intygsRef);

        return fs;
    }

    private Utkast createUtkast(String intygId) {

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId("SE12345678-0000");
        vardperson.setNamn("Dr Börje Dengroth");

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp("fk7263");
        utkast.setEnhetsId("SE12345678-1000");
        utkast.setEnhetsNamn("Vårdenhet 1");
        utkast.setPatientPersonnummer(new Personnummer("19121212-1212"));
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvansson");
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel("{model}");
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }

}
