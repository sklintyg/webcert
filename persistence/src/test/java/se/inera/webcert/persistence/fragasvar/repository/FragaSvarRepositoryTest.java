package se.inera.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles("dev")
@Transactional
public class FragaSvarRepositoryTest {
    
    @Autowired
    private CrudRepository<FragaSvar, Long> fragasvarRepository;
    
    @PersistenceContext
    private EntityManager em;
    
    private LocalDateTime FRAGE_SIGN_DATE = new LocalDateTime("2013-03-01T11:11:11");
    private LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2013-03-01T12:00:00");
    private IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk", "Sven Persson", "19121212-1212", FRAGE_SENT_DATE);
   
    @Test
    public void testFindOne() {
        FragaSvar saved = buildFragaSvarFraga();
       
        
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

    private FragaSvar buildFragaSvarFraga() {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(Arrays.asList("KONTAKT1","KONTAKT2","KONTAKT3"));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGE_SENT_DATE);
        f.setFrageStallare("Olle");
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        
        return f;
    }

    

}
