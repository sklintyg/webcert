package se.inera.intyg.webcert.persistence.intyg.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import se.inera.intyg.webcert.persistence.intyg.repository.util.UtkastTestUtil;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class UtkastRepositoryTest {

    @Autowired
    private UtkastRepository utkastRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFindOne() {
        Utkast saved = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID));
        Utkast read = utkastRepository.findOne(saved.getIntygsId());

        assertThat(read.getIntygsId(), is(equalTo(saved.getIntygsId())));
        assertThat(read.getPatientPersonnummer(), is(equalTo(saved.getPatientPersonnummer())));
        assertThat(read.getPatientFornamn(), is(equalTo(saved.getPatientFornamn())));
        assertThat(read.getPatientMellannamn(), is(equalTo(saved.getPatientMellannamn())));
        assertThat(read.getPatientEfternamn(), is(equalTo(saved.getPatientEfternamn())));

        assertThat(read.getEnhetsId(), is(notNullValue()));

        assertThat(read.getModel(), is(equalTo(UtkastTestUtil.MODEL)));

        assertThat(read.getSignatur(), is(nullValue()));
    }

    @Test
    public void testFindOneWithSignature() {

        Utkast utkast = UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID);
        String intygsId = utkast.getIntygsId();
        utkast.setSignatur(UtkastTestUtil.buildSignatur(intygsId, "A", LocalDateTime.now()));

        Utkast saved = utkastRepository.save(utkast);
        Utkast read = utkastRepository.findOne(intygsId);

        assertThat(read.getIntygsId(), is(equalTo(saved.getIntygsId())));
        assertThat(read.getSignatur(), is(notNullValue()));
    }

    @Test
    public void testFindByEnhetsIdDontReturnSigned() {

        Utkast utkast1 = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        Utkast utkast2 = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        Utkast utkast3 = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.SIGNED));

        List<Utkast> result = utkastRepository.findByEnhetsIdsAndStatuses(Arrays.asList(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.ENHET_3_ID),
                Arrays.asList(UtkastStatus.DRAFT_COMPLETE));

        assertThat(result.size(), is(3));

        assertThat(utkast1, isIn(result));
        assertThat(utkast2, isIn(result));
        assertThat(utkast3, isIn(result));

    }

    @Test
    public void testCountIntygWithStatusesGroupedByEnhetsId() {

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.SIGNED));

        List<Object[]> result = utkastRepository.countIntygWithStatusesGroupedByEnhetsId(Arrays.asList(UtkastTestUtil.ENHET_1_ID),
                Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));
        assertThat(result.size(), is(1));

        Object[] resObjs = result.get(0);
        assertThat((String) resObjs[0], equalTo(UtkastTestUtil.ENHET_1_ID));
        assertThat((Long) resObjs[1], equalTo(3L));
    }

    @Test
    public void testFindDraftsByPatientAndEnhetAndStatus() {

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

        List<String> enhetsIds = Arrays.asList(UtkastTestUtil.ENHET_1_ID);
        List<UtkastStatus> statuses = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
        List<Utkast> results = utkastRepository.findDraftsByPatientAndEnhetAndStatus(UtkastTestUtil.PERSON_NUMMER.getPersonnummer(), enhetsIds, statuses,
                allIntygsTyper());

        assertThat(results.size(), is(2));

    }

    private Set<String> allIntygsTyper() {
        Set<String> set = new HashSet<>();
        set.add("fk7263");
        set.add("ts-bas");
        set.add("ts-diabetes");
        return set;
    }

    @Test
    public void testFindDistinctIntygHsaIdByEnhet() {

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.SIGNED, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.SIGNED, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-02"));

        List<UtkastStatus> statuses = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
        List<Object[]> res = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(UtkastTestUtil.ENHET_1_ID, statuses);

        assertThat(res.size(), is(2));
    }

    @Test
    public void testDelete() {

        utkastRepository.save(UtkastTestUtil.buildUtkast("intyg-1", UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

        utkastRepository.delete("intyg-1");

        assertThat(utkastRepository.findOne("intyg-1"), is(nullValue()));
    }

    @Test
    public void testGetIntygsStatus() {
        Utkast intyg3 = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
        UtkastStatus status = utkastRepository.getIntygsStatus(intyg3.getIntygsId());
        assertThat(status, is(UtkastStatus.DRAFT_COMPLETE));
    }

}
