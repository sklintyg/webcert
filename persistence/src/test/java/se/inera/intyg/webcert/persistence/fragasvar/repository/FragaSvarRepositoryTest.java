package se.inera.intyg.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Status;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class FragaSvarRepositoryTest {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @PersistenceContext
    private EntityManager em;

    private static final String INTYGS_ID = "abc123";

    private LocalDateTime FRAGA_SIGN_DATE = new LocalDateTime("2013-03-01T11:11:11");
    private LocalDateTime FRAGA_SENT_DATE = new LocalDateTime("2013-03-01T12:00:00");
    private LocalDateTime SVAR_SIGN_DATE = new LocalDateTime("2013-04-01T11:11:11");
    private LocalDateTime SVAR_SENT_DATE = new LocalDateTime("2013-04-01T12:00:00");

    private IntygsReferens INTYGS_REFERENS = new IntygsReferens(INTYGS_ID, "fk", new Personnummer("19121212-1212"),
            "Sven Persson", FRAGA_SENT_DATE);

    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

    private static String HSA_1_ID = "HSA_1_ID";
    private static String HSA_2_ID = "HSA_2_ID";
    private static String HSA_3_ID = "HSA_3_ID";
    private static String HSA_4_ID = "HSA_4_ID";

    private static String HSA_1_NAMN = "A HSA NAMN 1";
    private static String HSA_2_NAMN = "B HSA NAMN 2";
    private static String HSA_3_NAMN = "C HSA NAMN 3";
    private static String HSA_4_NAMN = "D HSA NAMN 4";

    @Test
    public void testFindOne() {
        FragaSvar saved = buildFragaSvarFraga(ENHET_1_ID);
        fragasvarRepository.save(saved);
        FragaSvar read = fragasvarRepository.findOne(saved.getInternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
        assertEquals(read.getAmne(), saved.getAmne());
        assertEquals(read.getExternReferens(), saved.getExternReferens());
        assertEquals(read.getFrageSigneringsDatum(), saved.getFrageSigneringsDatum());
        assertEquals(read.getFrageSkickadDatum(), saved.getFrageSkickadDatum());
        assertEquals(read.getFrageStallare(), saved.getFrageStallare());
        assertEquals(read.getFrageText(), saved.getFrageText());
        assertEquals(read.getIntygsReferens(), saved.getIntygsReferens());
    }

    @Test
    public void testFindByEnhetsId() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_4_ID));

        List<FragaSvar> result = fragasvarRepository.findByEnhetsId(Arrays.asList(ENHET_1_ID, ENHET_3_ID));
        assertEquals(3, result.size());

    }

    @Test
    public void testFindByEnhetsIdDontMatchClosed() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        List<FragaSvar> result = fragasvarRepository.findByEnhetsId(Arrays.asList(ENHET_1_ID, ENHET_2_ID, ENHET_3_ID));
        assertEquals(3, result.size());

    }

    @Test
    public void testcountUnhandledForEnhetsIds() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        long result = fragasvarRepository.countUnhandledForEnhetsIds(Arrays.asList(ENHET_1_ID, ENHET_2_ID));
        assertEquals(3, result);

    }

    @Test
    public void testcountUnhandledByEnhet() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        List<Object[]> res = fragasvarRepository.countUnhandledGroupedByEnhetIds(Arrays.asList(ENHET_1_ID, ENHET_2_ID));
        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test
    public void testFindByIntygsReferens() {
        FragaSvar saved = buildFragaSvarFraga(ENHET_1_ID);
        saved.setIntygsReferens(new IntygsReferens("non-existing-intygs-id", "fk", new Personnummer("19121212-1212"), "Sven Persson",
                FRAGA_SENT_DATE));
        fragasvarRepository.save(saved);
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_4_ID));

        List<FragaSvar> result = fragasvarRepository.findByIntygsReferensIntygsId(INTYGS_REFERENS.getIntygsId());
        assertEquals(2, result.size());

    }

    private FragaSvar buildFragaSvarFraga(String enhetsId) {
        return buildFragaSvarFraga(enhetsId, Status.PENDING_EXTERNAL_ACTION);
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status) {
        return buildFragaSvarFraga(enhetsId, status, false);
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, boolean answered) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare("Olle");
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        if (answered) {
            f.setSvarsText("Ett svar på frågan");
        }

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String hsaid, String hsaNamn) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare("Olle");
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        vardperson.setHsaId(hsaid);
        vardperson.setNamn(hsaNamn);
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String frageStallare) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare(frageStallare);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String frageStallare, boolean answered) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare(frageStallare);
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        if (answered) {
            f.setSvarsText("Ett svar på frågan");
        }

        return f;
    }

    @Test
    public void testFindByExternReferens() {
        FragaSvar saved = buildFragaSvarFraga("Enhet-1-id", Status.PENDING_EXTERNAL_ACTION);

        fragasvarRepository.save(saved);

        FragaSvar read = fragasvarRepository.findByExternReferens(saved.getExternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());

    }

    @Test
    public void testFragaSenasteHandelse() {
        FragaSvar saved = buildFragaSvarFraga("Enhet-1-id", Status.PENDING_EXTERNAL_ACTION);

        fragasvarRepository.save(saved);

        FragaSvar read = fragasvarRepository.findByExternReferens(saved.getExternReferens());

        assertEquals(read.getFrageSkickadDatum(), read.getSenasteHandelse());

        read.setSvarsText("svarstext");
        read.setSvarSkickadDatum(SVAR_SENT_DATE);
        read.setSvarSigneringsDatum(SVAR_SIGN_DATE);

        FragaSvar svar2 = fragasvarRepository.save(read);

        FragaSvar read2 = fragasvarRepository.findByExternReferens(svar2.getExternReferens());

        assertEquals(read2.getSvarSkickadDatum(), read2.getSenasteHandelse());

    }

    @Test
    public void testFindAllHSAIDByEnhet() {
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_1_ID, HSA_1_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.PENDING_INTERNAL_ACTION, HSA_4_ID, HSA_4_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));

        List<String> params = Arrays.asList(ENHET_1_ID, ENHET_2_ID);

        List<Object[]> lakare = fragasvarRepository.findDistinctFragaSvarHsaIdByEnhet(params);

        // Assert that we only get 3 items back.
        assertEquals(3, lakare.size());

        // Assert that no value is HSA_4_ID. Wrong Enhet
        for (int i = 0; i < lakare.size(); i++) {
            assertFalse(lakare.get(i)[0].equals(HSA_4_ID));
        }

        // Results should be sorted by name, so we should always get them in the same order.
        assertTrue(lakare.get(0)[0].equals(HSA_1_ID));
        assertTrue(lakare.get(1)[0].equals(HSA_2_ID));
        assertTrue(lakare.get(2)[0].equals(HSA_3_ID));
    }
}
