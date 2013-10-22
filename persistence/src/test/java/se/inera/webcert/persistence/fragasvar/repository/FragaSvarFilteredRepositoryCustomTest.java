package se.inera.webcert.persistence.fragasvar.repository;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.fragasvar.model.*;
import se.inera.webcert.persistence.fragasvar.repository.util.FragaSvarTestUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by pehr on 10/21/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles("dev")
@Transactional
public class FragaSvarFilteredRepositoryCustomTest {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void setUp(){
        //FragaSvarTestUtil.buildFragaSvar(10,fragasvarRepository);

        //System.out.println("COUNT :" + fragasvarRepository.count());
    }

    //@After
    public void closeDown(){
        fragasvarRepository.deleteAll();
        System.out.println("COUNT 333:" + fragasvarRepository.count());
    }

    @Test
    public void testFilterFragaFromWC() {


        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.buildFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        System.out.println("COUNT222 :"+ fsList.size());
        Assert.assertTrue(fsList.size() == 1);
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterFragaFromWCWithPaging() {


        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.buildFragaSvar(filter, 10,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter,new PageRequest(4,3));

        System.out.println("REPO-COUNT :" + fragasvarRepository.count());
        System.out.println("svar COUNT :"+ fsList.size());
        Assert.assertTrue(fsList.size()==3);
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterHsaId() {
        String hsaid =  "HSA-User-123";
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setHsaId(hsaid);
        FragaSvarTestUtil.buildFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        System.out.println("REPO-COUNT :" + fragasvarRepository.count());
        System.out.println("svar COUNT :"+ fsList.size());
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getVardperson().getHsaId().equals(hsaid));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterChangedAfter() {
        LocalDateTime changeDateFrom =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setChangedFrom(changeDateFrom);
        FragaSvarTestUtil.buildFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedFrom(filter.getChangedFrom().minusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        System.out.println("REPO-COUNT :" + fragasvarRepository.count());
        System.out.println("svar COUNT :"+ fsList.size());
        //Assert.assertTrue(fsList.size() == 1);
        fragasvarRepository.deleteAll();
    }

}
