package se.inera.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
public class CountFragaSvarRepositoryTest {

    private static final String FK = "FK";
    private static final String WC = "WC";

    private LocalDateTime FRAGE_SIGN_DATE = new LocalDateTime("2013-03-01T11:11:11");
    private LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2013-03-01T12:00:00");

    @Autowired
    private FragaSvarRepository fsRepo;

    @Test
    @Transactional
    public void testCountFragaSvarWithQuestionFromFK() {
        
        String intygsId = "intyg1";

        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));

        // Fråga mottages från FK: Händelsekod HAN6. antalFragor ökar med 1.
        // FragaSvar [frageStallare = FK, status = PENDING_INT]

        FragaSvar fs1 = buildFragaSvarFraga(Status.PENDING_INTERNAL_ACTION, FK, "intyg1");
        fs1 = fsRepo.save(fs1);

        assertEquals(new Long(1), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));

        // Fråga från FK hanteras i webcert, antingen genom att svar skickas eller frågan markeras som hanterad:
        // Händelsekod HAN9. antalHanteradeFragor ökar med 1.
        // FragaSvar [frageStallare = FK, status = CLOSED]

        fs1.setStatus(Status.CLOSED);
        fs1 = fsRepo.save(fs1);

        assertEquals(new Long(1), antalFragor(intygsId));
        assertEquals(new Long(1), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));

        // Fråga från FK som tidigare markerats som hanterad, avmarkeras
        // Händelsekod HAN9, antalHanteradeFragor minskar med 1.
        // FragaSvar [frageStallare = FK, status = PENDING_INT]

        fs1.setStatus(Status.PENDING_INTERNAL_ACTION);
        fs1 = fsRepo.save(fs1);

        assertEquals(new Long(1), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));

    }

    @Test
    @Transactional
    public void testCountFragaSvarWithQuestionToFK() {
        
        String intygsId = "intyg2";
        
        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));
        
        // Fråga skickas från webcert till FK: Händelsekod HAN8. Inget värde räknas upp
        // FragaSvar [frageStallare = WC, status = PENDING_EXT]
        
        FragaSvar fs2 = buildFragaSvarFraga(Status.PENDING_EXTERNAL_ACTION, WC, intygsId);
        fs2 = fsRepo.save(fs2);
        
        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));
        
        // Svar mottages från FK: Händelsekod HAN7. antalSvar ökar med 1.
        // FragaSvar [frageStallare = WC, status = ANSWERED]
        fs2.setStatus(Status.ANSWERED);
        fs2 = fsRepo.save(fs2);
        
        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(1), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));
        
        // Svar från FK markeras som hanterat i webcert: Händelsekod HAN10. antalHanteradeSvar ökar med 1.
        // FragaSvar [frageStallare = WC, status = CLOSED]
        
        fs2.setStatus(Status.CLOSED);
        fs2 = fsRepo.save(fs2);
        
        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(0), antalSvar(intygsId));
        assertEquals(new Long(1), antalHanteradeSvar(intygsId));
        
        // Svar från FK som tidigare markerats som hanterat, avmarkeras. Händelsekod HAN10, antalHanteradeSvar minskar med 1.
        // FragaSvar [frageStallare = WC, status = ANSWERED]
        
        fs2.setStatus(Status.ANSWERED);
        fs2 = fsRepo.save(fs2);
        
        assertEquals(new Long(0), antalFragor(intygsId));
        assertEquals(new Long(0), antalHanteradeFragor(intygsId));
        assertEquals(new Long(1), antalSvar(intygsId));
        assertEquals(new Long(0), antalHanteradeSvar(intygsId));

    }

    private Long antalFragor(String intygsId) {
        return fsRepo.countByIntygAndFragestallare(intygsId, FK);
    }

    private Long antalHanteradeFragor(String intygsId) {
        return fsRepo.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, FK);
    }

    private Long antalSvar(String intygsId) {
        return fsRepo.countByIntygAndStatusAndFragestallare(intygsId, Status.ANSWERED, WC);
    }

    private Long antalHanteradeSvar(String intygsId) {
        return fsRepo.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, WC);
    }

    private FragaSvar buildFragaSvarFraga(Status status, String frageStallare, String intygsId) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGE_SENT_DATE);
        f.setFrageStallare(frageStallare);
        f.setFrageText("Detta var ju otydligt formulerat!");
        IntygsReferens intygsRef = new IntygsReferens(intygsId, "fk7263", "Sven Persson",
                FRAGE_SIGN_DATE);
        f.setIntygsReferens(intygsRef);
        f.setStatus(status);

        return f;
    }
}
