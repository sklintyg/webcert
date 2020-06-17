/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.event.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles({"dev", "unit-testing"})
@Transactional
public class UtkastEventRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UtkastEventRepository utkastEventRepository;

    @After
    public void cleanup() {
        utkastEventRepository.deleteAll();
    }

    @Test
    public void testFindOneByIntygsId() {
        UtkastEvent savedUtkastEvent = utkastEventRepository.save(UtkastEventTestUtil.buildUtkastEvent(UtkastEventTestUtil.INTYG_1_ID));
        List<UtkastEvent> foundUtkastEvent = utkastEventRepository.findByIntygsId(savedUtkastEvent.getIntygsId());

        assertFalse(foundUtkastEvent.isEmpty());
        assertEquals(1, foundUtkastEvent.size());
        assertEquals(savedUtkastEvent, foundUtkastEvent.get(0));
    }

    @Test
    public void testFindAllByIntygsId() {
        utkastEventRepository
            .save(UtkastEventTestUtil.buildUtkastEvent(UtkastEventTestUtil.INTYG_1_ID, UtkastEventTestUtil.EVENT_KOD_SKAPAT));
        utkastEventRepository
            .save(UtkastEventTestUtil.buildUtkastEvent(UtkastEventTestUtil.INTYG_1_ID, UtkastEventTestUtil.EVENT_KOD_SIGNAT));
        utkastEventRepository
            .save(UtkastEventTestUtil.buildUtkastEvent(UtkastEventTestUtil.INTYG_2_ID, UtkastEventTestUtil.EVENT_KOD_SKAPAT));

        List<UtkastEvent> foundUtkastEvent1 = utkastEventRepository.findByIntygsId(UtkastEventTestUtil.INTYG_1_ID);
        List<UtkastEvent> foundUtkastEvent2 = utkastEventRepository.findByIntygsId(UtkastEventTestUtil.INTYG_2_ID);
        //assertFalse(foundUtkastEvent1.isEmpty());
        assertFalse(foundUtkastEvent2.isEmpty());
        //assertEquals(2, foundUtkastEvent1.size());
        assertEquals(1, foundUtkastEvent2.size());

    }

    @Test
    public void testDeleteUtkastEventByIntygId() {
        UtkastEvent event1 = utkastEventRepository.save(UtkastEventTestUtil.buildUtkastEvent(UtkastEventTestUtil.INTYG_1_ID));

        utkastEventRepository.delete(event1);
        List<UtkastEvent> found = utkastEventRepository.findByIntygsId(UtkastEventTestUtil.INTYG_1_ID);
        assertTrue(found.isEmpty());
    }

}
