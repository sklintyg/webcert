package se.inera.webcert.persistence.fragasvar.repository;

import org.joda.time.LocalDateTime;
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

    @Test
    public void testFilterFragaFromWC() {
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getFrageStallare().equalsIgnoreCase("WC"));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterFragaFromWCWithPaging() {
        FragaSvarFilter filter = new FragaSvarFilter();

        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 10,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter,new PageRequest(4,3));

        Assert.assertTrue(fsList.size()==3);
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterHsaId() {
        String hsaid =  "HSA-User-123";
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setHsaId(hsaid);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getVardperson().getHsaId().equals(hsaid));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterChangedAfter() {
        LocalDateTime changeDateFrom =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setChangedFrom(changeDateFrom);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedFrom(filter.getChangedFrom().minusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        //Assert.assertTrue(fsList.size() == 1);
        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterChangedTo() {
        LocalDateTime changeDateTo =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();


        filter.setChangedTo(changeDateTo);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedTo(filter.getChangedTo().plusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        //Assert.assertTrue(fsList.size() == 1);
        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterChangedFrom() {
        LocalDateTime changeDateFrom =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();

        filter.setChangedFrom(changeDateFrom);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedFrom(filter.getChangedFrom().minusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        //We should only have one that matches the filter
        Assert.assertTrue(fsList.size() == 1);
        //The SenasteHandelse should be set automatically from the SvarsDatum.
        Assert.assertTrue(fsList.get(0).getSenasteHandelseDatum().equals(changeDateFrom));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterVidarebefordrad() {
        FragaSvarFilter filter = new FragaSvarFilter();

        filter.setVidarebefordrad(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

}
