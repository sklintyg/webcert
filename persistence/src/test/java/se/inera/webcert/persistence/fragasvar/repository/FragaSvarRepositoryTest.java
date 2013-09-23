package se.inera.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles("dev")
@Transactional
public class FragaSvarRepositoryTest {
    
    @Autowired
    private CrudRepository<FragaSvar, Long> fragasvarRepository;
    
    @PersistenceContext
    private EntityManager em;
    
    @Test
    public void testFindOne() {
        FragaSvar fragasvar = new FragaSvar();
        fragasvar.setAmne(Amne.AVSTAMNINGSMOTE);
        fragasvar.setExternReferens("externReferens");
        
        fragasvarRepository.save(fragasvar);
        System.out.println("generated ID is:" + fragasvar.getInternReferens());
        FragaSvar read = fragasvarRepository.findOne(fragasvar.getInternReferens());
        assertEquals(read.getInternReferens(), fragasvar.getInternReferens());
        assertEquals(read.getAmne(), fragasvar.getAmne());
        assertEquals(read.getExternReferens(), fragasvar.getExternReferens());
        
    }

}
