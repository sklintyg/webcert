/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.persistence.utkast.repository.UtkastTestUtil.PERSON_NUMMER;

import com.google.common.collect.Sets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UtkastRepositoryTest {

  @Autowired private UtkastRepository utkastRepository;

  @PersistenceContext private EntityManager em;

  @Test
  void testFindOne() {
    Utkast saved = utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID));
    Utkast read = utkastRepository.findById(saved.getIntygsId()).orElse(null);

    assertNotNull(read);
    assertEquals(saved.getIntygsId(), read.getIntygsId());
    assertEquals(saved.getPatientPersonnummer(), read.getPatientPersonnummer());
    assertEquals(saved.getPatientFornamn(), read.getPatientFornamn());
    assertEquals(saved.getPatientMellannamn(), read.getPatientMellannamn());
    assertEquals(saved.getPatientEfternamn(), read.getPatientEfternamn());

    assertNotNull(read.getEnhetsId());

    assertEquals(UtkastTestUtil.MODEL, read.getModel());

    assertNull(read.getSignatur());
  }

  @Test
  void testFindOneWithSignature() {

    Utkast utkast = UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID);
    String intygsId = utkast.getIntygsId();
    utkast.setSignatur(UtkastTestUtil.buildSignatur(intygsId, "A", LocalDateTime.now()));

    Utkast saved = utkastRepository.save(utkast);
    Utkast read = utkastRepository.findById(intygsId).orElse(null);

    assertNotNull(read);
    assertEquals(saved.getIntygsId(), read.getIntygsId());
    assertNotNull(read.getSignatur());
  }

  @Test
  void testFindByEnhetsIdDontReturnSigned() {

    Utkast utkast1 =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
    Utkast utkast2 =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
    Utkast utkast3 =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.SIGNED));

    List<Utkast> result =
        utkastRepository.findByEnhetsIdsAndStatuses(
            Arrays.asList(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.ENHET_3_ID),
            Collections.singletonList(UtkastStatus.DRAFT_COMPLETE));

    assertEquals(3, result.size());

    assertTrue(result.contains(utkast1));
    assertTrue(result.contains(utkast2));
    assertTrue(result.contains(utkast3));
  }

  @Test
  void testCountIntygWithStatusesGroupedByEnhetsId() {

    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.SIGNED));

    List<GroupableItem> result =
        utkastRepository.getIntygWithStatusesByEnhetsId(
            Collections.singletonList(UtkastTestUtil.ENHET_1_ID),
            UtkastStatus.getEditableDraftStatuses(),
            Stream.of(UtkastTestUtil.INTYGSTYP_FK7263)
                .collect(Collectors.toCollection(HashSet::new)));
    assertEquals(3, result.size());

    GroupableItem resObjs = result.get(0);
    assertEquals(UtkastTestUtil.ENHET_1_ID, resObjs.getEnhetsId());
    assertEquals(PERSON_NUMMER.getPersonnummerWithDash(), resObjs.getPersonnummer());
    assertEquals(resObjs.getIntygsTyp(), "fk7263");
  }

  @Test
  void testFindDraftsByPatientAndEnhetAndStatus() {

    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

    List<String> enhetsIds = Collections.singletonList(UtkastTestUtil.ENHET_1_ID);
    List<UtkastStatus> statuses =
        Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
    List<Utkast> results =
        utkastRepository.findDraftsByPatientAndEnhetAndStatus(
            PERSON_NUMMER.getPersonnummerWithDash(), enhetsIds, statuses, allIntygsTyper());

    assertEquals(2, results.size());
  }

  @Test
  void testFindDraftsByPatientAndVardgivarIdAndStatus() {

    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastStatus.DRAFT_COMPLETE));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

    String vardgivarId = UtkastTestUtil.ENHET_1_ID;
    List<UtkastStatus> statuses =
        Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE);
    List<Utkast> results =
        utkastRepository.findDraftsByPatientAndVardgivareAndStatus(
            PERSON_NUMMER.getPersonnummerWithDash(), vardgivarId, statuses, allIntygsTyper());

    assertEquals(2, results.size());
  }

  private Set<String> allIntygsTyper() {
    Set<String> set = new HashSet<>();
    set.add("fk7263");
    set.add("ts-bas");
    set.add("ts-diabetes");
    return set;
  }

  @Test
  void testFindDistinctIntygHsaIdByEnhet() {

    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID,
            UtkastTestUtil.HOS_PERSON2_ID,
            UtkastTestUtil.HOS_PERSON2_NAMN,
            UtkastStatus.SIGNED,
            "2014-03-01"));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_2_ID,
            UtkastTestUtil.HOS_PERSON3_ID,
            UtkastTestUtil.HOS_PERSON3_NAMN,
            UtkastStatus.DRAFT_COMPLETE,
            "2014-03-01"));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID,
            UtkastTestUtil.HOS_PERSON1_ID,
            UtkastTestUtil.HOS_PERSON1_NAMN,
            UtkastStatus.DRAFT_INCOMPLETE,
            "2014-03-01"));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_2_ID,
            UtkastTestUtil.HOS_PERSON1_ID,
            UtkastTestUtil.HOS_PERSON1_NAMN,
            UtkastStatus.SIGNED,
            "2014-03-02"));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID,
            UtkastTestUtil.HOS_PERSON2_ID,
            UtkastTestUtil.HOS_PERSON2_NAMN,
            UtkastStatus.DRAFT_COMPLETE,
            "2014-03-02"));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_2_ID,
            UtkastTestUtil.HOS_PERSON3_ID,
            UtkastTestUtil.HOS_PERSON3_NAMN,
            UtkastStatus.DRAFT_INCOMPLETE,
            "2014-03-02"));

    List<Object[]> res =
        utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(
            UtkastTestUtil.ENHET_1_ID, UtkastStatus.getEditableDraftStatuses());

    assertEquals(2, res.size());
  }

  @Test
  void testDelete() {

    Utkast intyg1 =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(
                "intyg-1", UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE));

    utkastRepository.delete(intyg1);
    Utkast one = utkastRepository.findById("intyg-1").orElse(null);
    assertNull(one);
  }

  @Test
  void testGetIntygsStatus() {
    Utkast intyg3 =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastStatus.DRAFT_COMPLETE));
    UtkastStatus status = utkastRepository.getIntygsStatus(intyg3.getIntygsId());
    assertEquals(UtkastStatus.DRAFT_COMPLETE, status);
  }

  @Test
  void testSaveRelation() {
    final String relationIntygsId = "relationIntygsId";
    final RelationKod relationKod = RelationKod.FRLANG;
    Utkast saved =
        utkastRepository.save(
            UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, relationIntygsId, relationKod));
    Optional<Utkast> readItem = utkastRepository.findById(saved.getIntygsId());
    Utkast read = readItem.orElse(null);

    assertNotNull(read);
    assertEquals(UtkastTestUtil.ENHET_1_ID, read.getEnhetsId());
    assertEquals(relationIntygsId, read.getRelationIntygsId());
    assertEquals(relationKod, read.getRelationKod());
  }

  @Test
  void testFindAllByRelationIntygsId() {
    String relationIntygsId = "parentCertificate";
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID, "someOtherCertificate", RelationKod.KOMPLT));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(
            UtkastTestUtil.ENHET_1_ID, relationIntygsId, RelationKod.KOMPLT));
    List<Utkast> res = utkastRepository.findAllByRelationIntygsId(relationIntygsId);

    assertNotNull(res);
    assertEquals(3, res.size());
  }

  @Test
  void testFindAllByRelationIntygsIdNoMatches() {
    List<Utkast> res = utkastRepository.findAllByRelationIntygsId("parentCertificate");
    assertNotNull(res);
    assertTrue(res.isEmpty());
  }

  @Test
  void testFindOneByIntygsIdAndIntygsTyp() {
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
  void testFindOneByIntygsIdAndIntygsTypNotFound() {
    final String intygsId = "intygsId";
    final String intygsTyp = "intygsTyp";

    Utkast res = utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, intygsTyp);
    assertNull(res);
  }

  @Test
  void testFindOneByIntygsIdAndIntygsTypInvalidIntygstyp() {
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
  void testfindAllByPatientPersonnummerAndIntygsTypIn() {
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.INTYGSTYP_FK7263));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.INTYGSTYP_LISJP));
    utkastRepository.save(
        UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_3_ID, UtkastTestUtil.INTYGSTYP_DB));

    List<Utkast> res =
        utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(
            UtkastTestUtil.PERSON_NUMMER.getPersonnummerWithDash(),
            Sets.newHashSet(UtkastTestUtil.INTYGSTYP_FK7263, UtkastTestUtil.INTYGSTYP_DB));
    assertNotNull(res);
    assertEquals(2, res.size());
  }
}
