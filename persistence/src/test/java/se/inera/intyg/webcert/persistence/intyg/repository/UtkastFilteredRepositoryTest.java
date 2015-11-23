package se.inera.intyg.webcert.persistence.intyg.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import se.inera.intyg.webcert.persistence.intyg.repository.util.UtkastTestUtil;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class UtkastFilteredRepositoryTest {

    @Autowired
    private UtkastRepository utkastRepository;

    @Before
    public void setup() {
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.SIGNED, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.SIGNED, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.SIGNED, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.SIGNED, "2014-03-04"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-04"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-04"));
    }

    @Test
    public void testFindWithEmptyFilter() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(6, res.size());
    }

    @Test
    public void testFindWithHsaId() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(5, res.size());
    }

    @Test
    public void testFindWithChangedFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-03"));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(3, res.size());
    }

    @Test
    public void testFindWithChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_2_ID);
        filter.setSavedTo(LocalDateTime.parse("2014-03-03"));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(4, res.size());
    }

    @Test
    public void testFindWithChangedFromAndChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-03"));
        filter.setSavedTo(LocalDateTime.parse("2014-03-04"));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(3, res.size());
    }

    @Test
    public void testFindWithStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(4, res.size());
    }

    @Test
    public void testFindWithHsaIdAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(2, res.size());
    }

    @Test
    public void testFindWithHsaIdAndDatesAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-02"));
        filter.setSavedTo(LocalDateTime.parse("2014-03-03"));
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(1, res.size());
    }

    @Test
    public void testFindWithPageSizeAndStartFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setPageSize(2);
        filter.setStartFrom(2);

        List<Utkast> res = utkastRepository.filterIntyg(filter);

        assertEquals(2, res.size());

        // Should return the third one for ENHET_1_ID
        Utkast intyg = res.get(0);
        assertNotNull(intyg);
        assertEquals(UtkastTestUtil.ENHET_1_ID, intyg.getEnhetsId());
        assertEquals(UtkastTestUtil.HOS_PERSON2_ID, intyg.getSenastSparadAv().getHsaId());

    }

    @Test
    public void testCountWithEmptyFilter() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(6, res);
    }

    @Test
    public void testCountWithHsaId() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(5, res);
    }

    @Test
    public void testCountWithChangedFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-03"));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(3, res);
    }

    @Test
    public void testCountWithChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_2_ID);
        filter.setSavedTo(LocalDateTime.parse("2014-03-03"));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(4, res);
    }

    @Test
    public void testCountWithChangedFromAndChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-03"));
        filter.setSavedTo(LocalDateTime.parse("2014-03-04"));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(3, res);
    }

    @Test
    public void testCountWithStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(4, res);
    }

    @Test
    public void testCountWithHsaIdAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(2, res);
    }

    @Test
    public void testCountWithHsaIdAndDatesAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setSavedFrom(LocalDateTime.parse("2014-03-02"));
        filter.setSavedTo(LocalDateTime.parse("2014-03-03"));
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        int res = utkastRepository.countFilterIntyg(filter);

        assertEquals(1, res);
    }
}
