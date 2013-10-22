package se.inera.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles("dev")
@Transactional
public class FragaSvarRepositoryTest {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @PersistenceContext
    private EntityManager em;

    private LocalDateTime FRAGE_SIGN_DATE = new LocalDateTime("2013-03-01T11:11:11");
    private LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2013-03-01T12:00:00");
    private LocalDateTime SVAR_SIGN_DATE = new LocalDateTime("2013-04-01T11:11:11");
    private LocalDateTime SVAR_SENT_DATE = new LocalDateTime("2013-04-01T12:00:00");
    private IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk", "Sven Persson",
            FRAGE_SENT_DATE);
    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

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
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID,Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID,Status.CLOSED));
        

        List<FragaSvar> result = fragasvarRepository.findByEnhetsId(Arrays.asList(ENHET_1_ID,ENHET_2_ID, ENHET_3_ID));
        assertEquals(3, result.size());

    }
    
    @Test
    public void testFindByIntygsReferens() {
        FragaSvar saved = buildFragaSvarFraga(ENHET_1_ID);
        saved.setIntygsReferens(new IntygsReferens("non-existing-intygs-id", "fk", "Sven Persson",
                FRAGE_SENT_DATE));
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
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGE_SENT_DATE);
        f.setFrageStallare("Olle");
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

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
}
