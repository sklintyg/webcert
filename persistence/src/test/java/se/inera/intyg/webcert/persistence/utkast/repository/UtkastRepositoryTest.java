/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static se.inera.intyg.webcert.persistence.utkast.repository.UtkastTestUtil.PERSON_NUMMER;

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

        List<Utkast> result = utkastRepository.findByEnhetsIdsAndStatuses(
                Arrays.asList(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.ENHET_3_ID),
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

        List<GroupableItem> result = utkastRepository.getIntygWithStatusesByEnhetsId(
                Arrays.asList(UtkastTestUtil.ENHET_1_ID),
                UtkastStatus.getEditableDraftStatuses(),
                Stream.of(UtkastTestUtil.INTYGSTYP_FK7263).collect(Collectors.toCollection(HashSet::new)));
        assertThat(result.size(), is(3));

        GroupableItem resObjs = result.get(0);
        assertThat(resObjs.getEnhetsId(), equalTo(UtkastTestUtil.ENHET_1_ID));
        assertThat(resObjs.getPersonnummer(), equalTo(PERSON_NUMMER.getPersonnummerWithDash()));
        assertThat(resObjs.getIntygsTyp(), equalTo("fk7263"));
    }

    @Test
    public void testFindDraftsByPatientAndEnhetAndStatus() {

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

        List<String> enhetsIds = Arrays.asList(UtkastTestUtil.ENHET_1_ID);
        List<UtkastStatus> statuses = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
        List<Utkast> results = utkastRepository.findDraftsByPatientAndEnhetAndStatus(PERSON_NUMMER.getPersonnummerWithDash(),
                enhetsIds, statuses, allIntygsTyper());

        assertThat(results.size(), is(2));

    }

    @Test
    public void testFindDraftsByPatientAndVardgivarIdAndStatus() {

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

        String vardgivarId = UtkastTestUtil.ENHET_1_ID;
        List<UtkastStatus> statuses = Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
        List<Utkast> results = utkastRepository.findDraftsByPatientAndVardgivareAndStatus(PERSON_NUMMER.getPersonnummerWithDash(),
                vardgivarId, statuses, allIntygsTyper());

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

        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID,
                UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.SIGNED, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID,
                UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON1_ID,
                UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID,
                UtkastTestUtil.HOS_PERSON1_NAMN, UtkastStatus.SIGNED, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID,
                UtkastTestUtil.HOS_PERSON2_NAMN, UtkastStatus.DRAFT_COMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID,
                UtkastTestUtil.HOS_PERSON3_NAMN, UtkastStatus.DRAFT_INCOMPLETE, "2014-03-02"));

        List<Object[]> res = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(UtkastTestUtil.ENHET_1_ID,
                UtkastStatus.getEditableDraftStatuses());

        assertThat(res.size(), is(2));
    }

    @Test
    public void testDelete() {

        utkastRepository.save(UtkastTestUtil.buildUtkast("intyg-1", UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

        utkastRepository.delete("intyg-1");
        Utkast one = utkastRepository.findOne("intyg-1");
        assertNull(one);
    }

    @Test
    public void testGetIntygsStatus() {
        Utkast intyg3 = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
        UtkastStatus status = utkastRepository.getIntygsStatus(intyg3.getIntygsId());
        assertThat(status, is(UtkastStatus.DRAFT_COMPLETE));
    }

    @Test
    public void testSaveRelation() {
        final String relationIntygsId = "relationIntygsId";
        final RelationKod relationKod = RelationKod.FRLANG;
        Utkast saved = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, relationIntygsId, relationKod));
        Utkast read = utkastRepository.findOne(saved.getIntygsId());

        assertEquals(UtkastTestUtil.ENHET_1_ID, read.getEnhetsId());
        assertEquals(relationIntygsId, read.getRelationIntygsId());
        assertEquals(relationKod, read.getRelationKod());
    }

    @Test
    public void testFindAllByRelationIntygsId() {
        String relationIntygsId = "parentCertificate";
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, "someOtherCertificate", RelationKod.KOMPLT));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
        List<Utkast> res = utkastRepository.findAllByRelationIntygsId(relationIntygsId);

        assertNotNull(res);
        assertEquals(3, res.size());
    }

    @Test
    public void testFindAllByRelationIntygsIdNoMatches() {
        List<Utkast> res = utkastRepository.findAllByRelationIntygsId("parentCertificate");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testFindOneByIntygsIdAndIntygsTyp() {
        final String intygsId = "intygsId";
        final String intygsTyp = "intygsTyp";
        Utkast utkast = UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(intygsTyp);
        utkastRepository.save(utkast);

        Utkast res = utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, intygsTyp);
        assertNotNull(res);
    }

    @Test
    public void testFindOneByIntygsIdAndIntygsTypNotFound() {
        final String intygsId = "intygsId";
        final String intygsTyp = "intygsTyp";

        Utkast res = utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, intygsTyp);
        assertNull(res);
    }

    @Test
    public void testFindOneByIntygsIdAndIntygsTypInvalidIntygstyp() {
        final String intygsId = "intygsId";
        final String intygsTyp = "intygsTyp";
        Utkast utkast = UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(intygsTyp);
        utkastRepository.save(utkast);

        Utkast res = utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, "anotherIntygsTyp");
        assertNull(res);
    }

    @Test
    public void testfindAllByPatientPersonnummerAndIntygsTypIn() {
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.INTYGSTYP_FK7263));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.INTYGSTYP_LISJP));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastTestUtil.INTYGSTYP_DB));

        List<Utkast> res = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(UtkastTestUtil.PERSON_NUMMER.getPersonnummerWithDash(),
                Sets.newHashSet(UtkastTestUtil.INTYGSTYP_FK7263, UtkastTestUtil.INTYGSTYP_DB));
        assertNotNull(res);
        assertEquals(2, res.size());
    }
}
