package se.inera.webcert.persistence.intyg.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
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

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.util.IntygTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({"dev","unit-testing"})
@Transactional
public class IntygRepositoryTest {

    @Autowired
    private IntygRepository intygRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFindOne() {
        Intyg saved = intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID));
        Intyg read = intygRepository.findOne(saved.getIntygsId());

        assertThat(read.getIntygsId(), is(equalTo(saved.getIntygsId())));
        assertThat(read.getPatientPersonnummer(), is(equalTo(saved.getPatientPersonnummer())));
        assertThat(read.getPatientFornamn(), is(equalTo(saved.getPatientFornamn())));
        assertThat(read.getPatientMellannamn(), is(equalTo(saved.getPatientMellannamn())));
        assertThat(read.getPatientEfternamn(), is(equalTo(saved.getPatientEfternamn())));

        assertThat(read.getEnhetsId(), is(notNullValue()));

        assertThat(read.getModel(), is(equalTo(IntygTestUtil.MODEL)));
        
        assertThat(read.getSignatur(), is(nullValue()));
    }
    
    @Test
    public void testFindOneWithSignature() {
        
        Intyg intyg = IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID);
        String intygsId = intyg.getIntygsId();
        intyg.setSignatur(IntygTestUtil.buildSignatur(intygsId, "A", LocalDateTime.now()));
        
        Intyg saved = intygRepository.save(intyg);
        Intyg read = intygRepository.findOne(intygsId);

        assertThat(read.getIntygsId(), is(equalTo(saved.getIntygsId())));
        assertThat(read.getSignatur(), is(notNullValue()));
    }
    
    @Test
    public void testFindByEnhetsIdDontReturnSigned() {

        Intyg intyg1 = intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_COMPLETE));
        Intyg intyg2 = intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_COMPLETE));
        Intyg intyg3 = intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_3_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.SIGNED));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygsStatus.SIGNED));

        List<Intyg> result = intygRepository.findByEnhetsIdsAndStatuses(Arrays.asList(IntygTestUtil.ENHET_1_ID, IntygTestUtil.ENHET_3_ID),
                Arrays.asList(IntygsStatus.DRAFT_COMPLETE));

        assertThat(result.size(), is(3));

        assertThat(intyg1, isIn(result));
        assertThat(intyg2, isIn(result));
        assertThat(intyg3, isIn(result));

    }

    @Test
    public void testCountIntygWithStatusesGroupedByEnhetsId() {

        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_INCOMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_3_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_INCOMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_3_ID, IntygsStatus.SIGNED));

        List<Object[]> result = intygRepository.countIntygWithStatusesGroupedByEnhetsId(Arrays.asList(IntygTestUtil.ENHET_1_ID), Arrays.asList(IntygsStatus.DRAFT_COMPLETE, IntygsStatus.DRAFT_INCOMPLETE));
        assertThat(result.size(), is(1));

        Object[] resObjs = result.get(0);
        assertThat((String) resObjs[0], equalTo(IntygTestUtil.ENHET_1_ID));
        assertThat((Long) resObjs[1], equalTo(3L));
    }

    @Test
    public void testFindDraftsByPatientAndEnhetAndStatus() {

        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.SIGNED));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_INCOMPLETE));

        List<String> enhetsIds = Arrays.asList(IntygTestUtil.ENHET_1_ID);
        List<IntygsStatus> statuses = Arrays.asList(IntygsStatus.DRAFT_COMPLETE, IntygsStatus.DRAFT_INCOMPLETE);
        List<Intyg> results = intygRepository.findDraftsByPatientAndEnhetAndStatus(IntygTestUtil.PERSON_NUMMER, enhetsIds, statuses);

        assertThat(results.size(), is(2));

    }

    @Test
    public void testFindDistinctIntygHsaIdByEnhet() {

        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.SIGNED, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON3_ID, IntygTestUtil.HOS_PERSON3_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON1_ID, IntygTestUtil.HOS_PERSON1_NAMN, IntygsStatus.SIGNED, "2014-03-02"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_1_ID, IntygTestUtil.HOS_PERSON2_ID, IntygTestUtil.HOS_PERSON2_NAMN, IntygsStatus.DRAFT_COMPLETE, "2014-03-02"));
        intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_2_ID, IntygTestUtil.HOS_PERSON3_ID, IntygTestUtil.HOS_PERSON3_NAMN, IntygsStatus.DRAFT_INCOMPLETE, "2014-03-02"));

        List<IntygsStatus> statuses = Arrays.asList(IntygsStatus.DRAFT_COMPLETE, IntygsStatus.DRAFT_INCOMPLETE);
        List<Object[]> res = intygRepository.findDistinctLakareFromIntygEnhetAndStatuses(IntygTestUtil.ENHET_1_ID, statuses);

        assertThat(res.size(), is(2));
    }

    @Test
    public void testDelete() {

        intygRepository.save(IntygTestUtil.buildIntyg("intyg-1", IntygTestUtil.ENHET_1_ID, IntygsStatus.DRAFT_INCOMPLETE));

        intygRepository.delete("intyg-1");

        assertThat(intygRepository.findOne("intyg-1"), is(nullValue()));
    }

    @Test
    public void testGetIntygsStatus() {
        Intyg intyg3 = intygRepository.save(IntygTestUtil.buildIntyg(IntygTestUtil.ENHET_3_ID, IntygsStatus.DRAFT_COMPLETE));
        IntygsStatus status = intygRepository.getIntygsStatus(intyg3.getIntygsId());
        assertThat(status, is(IntygsStatus.DRAFT_COMPLETE));
    }

}
