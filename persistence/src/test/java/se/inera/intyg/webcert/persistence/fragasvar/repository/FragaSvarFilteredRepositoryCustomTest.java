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
package se.inera.intyg.webcert.persistence.fragasvar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.repository.util.FragaSvarTestUtil;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.model.VantarPa;

/**
 * Test for filtering FragaSvar.
 *
 * @author nikpet
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class FragaSvarFilteredRepositoryCustomTest {

  private static final String WEBCERT = "WC";
  private static final String FK = "FK";

  private static final String HSA_ID_1 = "hsaId-1";
  private static final String HSA_ID_2 = "hsaId-2";

  @Autowired private FragaSvarRepository fragasvarRepository;

  @Test
  void testFilterFragaFromWC() {

    Filter filter = buildDefaultFilter();

    filter.setQuestionFromWC(true);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(5, fsList.size());
  }

  private Filter buildDefaultFilter() {
    Filter filter = new Filter();
    filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
    filter.getIntygsTyper().add("fk7263");
    return filter;
  }

  /** Should filter all FS that belongs to ENHET_2 and is not CLOSED. */
  @Test
  void testCountFilterFraga() {

    Filter filter = new Filter();
    filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_2_ID);
    filter.getIntygsTyper().add("fk7263");

    int res = fragasvarRepository.filterCountFragaSvar(filter);
    Assertions.assertEquals(3, res);
  }

  /** Should filter all FS that belongs to ENHET_1 and is not CLOSED. */
  @Test
  void testFilterFragaWithPaging() {

    Filter filter = buildDefaultFilter();
    filter.setPageSize(10);
    filter.setStartFrom(0);

    int res = fragasvarRepository.filterCountFragaSvar(filter);
    Assertions.assertEquals(14, res);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
    Assertions.assertEquals(10, fsList.size());
  }

  @Test
  void testFilterHsaId() {

    Filter filter = buildDefaultFilter();
    filter.setHsaId(HSA_ID_2);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(3, fsList.size());
  }

  @Test
  void testFilterChangedFrom() {

    Filter filter = buildDefaultFilter();
    filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:10:00"));

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(6, fsList.size());
  }

  @Test
  void testFilterChangedTo() {

    Filter filter = buildDefaultFilter();
    filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(8, fsList.size());
  }

  /** Should filter out all FS that is not CLOSED and has <= changedFrom and >= changedTo. */
  @Test
  void testFilterChangedBetween() {

    Filter filter = buildDefaultFilter();
    filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:00:00"));
    filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(8, fsList.size());
  }

  @Test
  void testFilterVidarebefordrad() {

    Filter filter = buildDefaultFilter();
    filter.setVidarebefordrad(true);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
    Assertions.assertEquals(3, fsList.size());
  }

  /**
   * Should return FS with status PENDING_INTERNAL_ACTION and a subject like OVRIGT,
   * ARBETSTIDSFORLAGGNING, AVSTAMNINGSMOTE, KONTAKT.
   */
  @Test
  void testFilterWaitingForReplyFromCare() {

    Filter filter = buildDefaultFilter();
    filter.setVantarPa(VantarPa.SVAR_FRAN_VARDEN);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
    Assertions.assertEquals(6, fsList.size());
  }

  /** Should return FS with status PENDING_EXTERNAL_ACTION */
  @Test
  void testFilterWaitingForReplyFromFK() {

    Filter filter = buildDefaultFilter();
    filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
    Assertions.assertEquals(4, fsList.size());
  }

  /**
   * Should return FS with status ANSWERED or (status PENDING_INTERNAL_ACTION and subject
   * MAKULERING_AV_LAKARINTYG ) or (status PENDING_INTERNAL_ACTION and subject PAMINNELSE).
   */
  @Test
  void testFilterMarkAsHandled() {

    Filter filter = buildDefaultFilter();
    filter.setVantarPa(VantarPa.MARKERA_SOM_HANTERAD);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(3, fsList.size());
  }

  /**
   * Should return FS with status PENDING_INTERNAL_ACTION and subject KOMPLETTERING_AV_LAKARINTYG.
   */
  @Test
  void testFilterVantaPaKomplettering() {

    Filter filter = buildDefaultFilter();
    filter.setVantarPa(VantarPa.KOMPLETTERING_FRAN_VARDEN);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(1, fsList.size());
  }

  /** Should return all FS that is not CLOSED. */
  @Test
  void testFilterAllNotHandled() {

    Filter filter = buildDefaultFilter();
    filter.setVantarPa(VantarPa.ALLA_OHANTERADE);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(14, fsList.size());
  }

  /**
   * Should filter out all FS with status PENDING_EXTERNAL_ACTION and is <= changedFrom and >=
   * changedTo.
   */
  @Test
  void testFilterChangedBetweenAndAwaitingReplyFromFK() {

    Filter filter = buildDefaultFilter();
    filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:03:00"));
    filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));
    filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(4, fsList.size());
  }

  /**
   * Should filter out all FS with status PENDING_EXTERNAL_ACTION that belongs to either ENHET_1_ID
   * or ENHET_2_ID.
   */
  @Test
  void testFilterWithTwoEnheterAndAwaitingReplyFromFK() {

    Filter filter = new Filter();
    filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_2_ID);
    filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
    filter.getIntygsTyper().add("fk7263");
    filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

    List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

    Assertions.assertEquals(7, fsList.size());
  }

  @Test
  void testFindFragaSvarStatusesForIntyg() {

    List<FragaSvarStatus> res = fragasvarRepository.findFragaSvarStatusesForIntyg("abc123");

    assertNotNull(res);
    assertEquals(20, res.size());
  }

  @BeforeEach
  void setupTestData() {

    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            1L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.CLOSED,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:01:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            2L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.OVRIGT,
            FK,
            HSA_ID_1,
            "2013-10-01T15:02:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            3L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_2,
            "2013-10-01T15:03:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            4L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.AVSTAMNINGSMOTE,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:04:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            5L,
            FragaSvarTestUtil.ENHET_2_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:04:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            6L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.ARBETSTIDSFORLAGGNING,
            FK,
            HSA_ID_1,
            "2013-10-01T15:05:00",
            true));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            7L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.AVSTAMNINGSMOTE,
            FK,
            HSA_ID_2,
            "2013-10-01T15:06:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            8L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:07:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            9L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.AVSTAMNINGSMOTE,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:08:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            10L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.KONTAKT,
            FK,
            HSA_ID_1,
            "2013-10-01T15:09:00",
            true));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            11L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.KONTAKT,
            FK,
            HSA_ID_2,
            "2013-10-01T15:10:00",
            true));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            12L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.AVSTAMNINGSMOTE,
            FK,
            HSA_ID_1,
            "2013-10-01T15:11:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            13L,
            FragaSvarTestUtil.ENHET_2_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:11:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            14L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.CLOSED,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:12:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            15L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.CLOSED,
            Amne.OVRIGT,
            FK,
            HSA_ID_1,
            "2013-10-01T15:13:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            16L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.KOMPLETTERING_AV_LAKARINTYG,
            FK,
            HSA_ID_1,
            "2013-10-01T15:14:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            17L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.ANSWERED,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_1,
            "2013-10-01T15:15:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            18L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.MAKULERING_AV_LAKARINTYG,
            FK,
            HSA_ID_1,
            "2013-10-01T15:16:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            19L,
            FragaSvarTestUtil.ENHET_1_ID,
            Status.PENDING_INTERNAL_ACTION,
            Amne.PAMINNELSE,
            FK,
            HSA_ID_1,
            "2013-10-01T15:17:00",
            false));
    fragasvarRepository.save(
        FragaSvarTestUtil.buildFraga(
            20L,
            FragaSvarTestUtil.ENHET_2_ID,
            Status.PENDING_EXTERNAL_ACTION,
            Amne.OVRIGT,
            WEBCERT,
            HSA_ID_2,
            "2013-10-01T15:18:00",
            false));
  }
}
