package se.inera.webcert.persistence.intyg.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.util.IntygTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles("dev")
@Transactional
public class IntygFilteredRepositoryTest {

    @Autowired
    private IntygRepository intygRepository;
    
    @Before
    public void setup() {
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.SIGNED, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON3_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.SIGNED, "2014-03-02"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-02"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON3_ID, IntygTestUtil.HOS_PERSON3_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-02"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.SIGNED, "2014-03-03"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-03"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-03"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON3_ID, IntygTestUtil.HOS_PERSON3_NAMN, IntygsStatus.SIGNED, "2014-03-04"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-04"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-04"));
    }
    
    @Test
    public void testFindWithEmptyFilter() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(6, res.size());
    }
    
    @Test
    public void testFindWithHsaId() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(IntygTestUtil.HOS_PERSON2_ID);
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(5, res.size());
    }
    
    @Test
    public void testFindWithChangedFrom() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setChangedFrom(LocalDateTime.parse("2014-03-03"));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(3, res.size());
    }
    
    @Test
    public void testFindWithChangedTo() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_2_ID);
        filter.setChangedTo(LocalDateTime.parse("2014-03-03"));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(4, res.size());
    }
    
    @Test
    public void testFindWithChangedFromAndChangedTo() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setChangedFrom(LocalDateTime.parse("2014-03-03"));
        filter.setChangedTo(LocalDateTime.parse("2014-03-04"));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(3, res.size());
    }
    
    @Test
    public void testFindWithStatuses() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setStatusList(Arrays.asList(IntygsStatus.DRAFT_COMPLETE, IntygsStatus.DRAFT_INCOMPLETE));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(4, res.size());
    }
    
    @Test
    public void testFindWithHsaIdAndStatuses() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(IntygTestUtil.HOS_PERSON2_ID);
        filter.setStatusList(Arrays.asList(IntygsStatus.SIGNED));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(2, res.size());
    }
    
    @Test
    public void testFindWithHsaIdAndDatesAndStatuses() {
        
        IntygFilter filter = new IntygFilter(IntygTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(IntygTestUtil.HOS_PERSON2_ID);
        filter.setChangedFrom(LocalDateTime.parse("2014-03-02"));
        filter.setChangedTo(LocalDateTime.parse("2014-03-03"));
        filter.setStatusList(Arrays.asList(IntygsStatus.SIGNED));
        
        List<Intyg> res = intygRepository.filterIntyg(filter);
        
        assertEquals(1, res.size());
    }
}
