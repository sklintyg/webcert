/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class UtkastFilteredRepositoryTest {

    @Autowired
    private UtkastRepository utkastRepository;

    Set<String> authorizedIntygstyper = Stream.of(UtkastTestUtil.INTYGSTYP_FK7263).collect(Collectors.toCollection(HashSet::new));

    @Before
    public void setup() {
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.SIGNED, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.DRAFT_COMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN,
                UtkastStatus.DRAFT_INCOMPLETE, "2014-03-01"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN,
                UtkastStatus.SIGNED, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.DRAFT_COMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN,
                UtkastStatus.DRAFT_INCOMPLETE, "2014-03-02"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.SIGNED, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN,
                UtkastStatus.DRAFT_COMPLETE, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.DRAFT_INCOMPLETE, "2014-03-03"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON3_ID, UtkastTestUtil.HOS_PERSON3_NAMN,
                UtkastStatus.SIGNED, "2014-03-04"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_1_ID, UtkastTestUtil.HOS_PERSON2_ID, UtkastTestUtil.HOS_PERSON2_NAMN,
                UtkastStatus.DRAFT_COMPLETE, "2014-03-04"));
        utkastRepository.save(UtkastTestUtil.buildUtkast(UtkastTestUtil.ENHET_2_ID, UtkastTestUtil.HOS_PERSON1_ID, UtkastTestUtil.HOS_PERSON1_NAMN,
                UtkastStatus.DRAFT_INCOMPLETE, "2014-03-04"));
    }

    @Test
    public void testFindWithEmptyFilter() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(6, res.size());
    }

    @Test
    public void testFindWithHsaId() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(5, res.size());
    }

    @Test
    public void testFindWithChangedFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-03").atStartOfDay());

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(3, res.size());
    }

    @Test
    public void testFindWithChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_2_ID);
        filter.setSavedTo(LocalDate.parse("2014-03-03").atStartOfDay());

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(4, res.size());
    }

    @Test
    public void testFindWithChangedFromAndChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-03").atStartOfDay());
        filter.setSavedTo(LocalDate.parse("2014-03-04").atStartOfDay());

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(3, res.size());
    }

    @Test
    public void testFindWithStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(4, res.size());
    }

    @Test
    public void testFindWithHsaIdAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(2, res.size());
    }

    @Test
    public void testFindWithHsaIdAndDatesAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-02").atStartOfDay());
        filter.setSavedTo(LocalDate.parse("2014-03-03").atStartOfDay());
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

        assertEquals(1, res.size());
    }

    @Test
    public void testFindWithPageSizeAndStartFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setPageSize(2);
        filter.setStartFrom(2);

        List<Utkast> res = utkastRepository.filterIntyg(filter, authorizedIntygstyper);

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

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(6, res);
    }

    @Test
    public void testCountWithHsaId() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(5, res);
    }

    @Test
    public void testCountWithChangedFrom() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-03").atStartOfDay());

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(3, res);
    }

    @Test
    public void testCountWithChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_2_ID);
        filter.setSavedTo(LocalDate.parse("2014-03-03").atStartOfDay());

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(4, res);
    }

    @Test
    public void testCountWithChangedFromAndChangedTo() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-03").atStartOfDay());
        filter.setSavedTo(LocalDate.parse("2014-03-04").atStartOfDay());

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(3, res);
    }

    @Test
    public void testCountWithStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(4, res);
    }

    @Test
    public void testCountWithHsaIdAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(2, res);
    }

    @Test
    public void testCountWithHsaIdAndDatesAndStatuses() {

        UtkastFilter filter = new UtkastFilter(UtkastTestUtil.ENHET_1_ID);
        filter.setSavedByHsaId(UtkastTestUtil.HOS_PERSON2_ID);
        filter.setSavedFrom(LocalDate.parse("2014-03-02").atStartOfDay());
        filter.setSavedTo(LocalDate.parse("2014-03-03").atStartOfDay());
        filter.setStatusList(Arrays.asList(UtkastStatus.SIGNED));

        int res = utkastRepository.countFilterIntyg(filter, authorizedIntygstyper);

        assertEquals(1, res);
    }
}
