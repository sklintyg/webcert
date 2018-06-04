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
package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by eriklupander on 2015-08-05.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class AvtalRepositoryTest {

    private static final String AVTAL_TEXT = "En väldigt lång avtalstext";
    private static final String HSA_ID = "userId1234";

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    private GodkantAvtalRepository godkantAvtalRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFindById() {
        Avtal saved = buildAvtal(1, AVTAL_TEXT);
        avtalRepository.save(saved);
        Avtal read = avtalRepository.findOne(saved.getAvtalVersion());
        assertEquals(read.getAvtalText(), AVTAL_TEXT);
    }

    @Test
    public void testGetLatestAvtalVersion() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        Avtal saved2 = buildAvtal(2, AVTAL_TEXT);
        Avtal saved3 = buildAvtal(3, AVTAL_TEXT);
        avtalRepository.save(saved2);
        avtalRepository.save(saved3);
        avtalRepository.save(saved1);

        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        assertEquals(3, latestAvtalVersion.intValue());
    }

    @Test
    public void testGetLatestAvtalVersionNoAvtalStored() {
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        assertEquals(-1, latestAvtalVersion.intValue());
    }

    @Test
    public void testUserHasNotApprovedAvtal() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        avtalRepository.save(saved1);
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertFalse(approved);
    }

    @Test
    public void testUserHasApprovedOldAvtal() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        Avtal saved2 = buildAvtal(2, AVTAL_TEXT);

        avtalRepository.save(saved1);
        avtalRepository.save(saved2);

        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();

        godkantAvtalRepository.approveAvtal(HSA_ID, 1);

        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertFalse(approved);
    }

    @Test
    public void testApproveAvtal() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        avtalRepository.save(saved1);
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();

        godkantAvtalRepository.approveAvtal(HSA_ID, latestAvtalVersion);

        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertTrue(approved);
    }

    @Test
    public void testApproveSameAvtalTwice() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        avtalRepository.save(saved1);
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();

        godkantAvtalRepository.approveAvtal(HSA_ID, latestAvtalVersion);

        // Applicaton code should stop this from triggering unique constraint
        godkantAvtalRepository.approveAvtal(HSA_ID, latestAvtalVersion);

        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertTrue(approved);
    }

    @Test
    public void testRemoveApprovedAvtal() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        avtalRepository.save(saved1);
        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        godkantAvtalRepository.approveAvtal(HSA_ID, latestAvtalVersion);

        godkantAvtalRepository.removeUserApprovement(HSA_ID, latestAvtalVersion);

        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertFalse(approved);
    }

    @Test
    public void testRemoveAllApprovedAvtalForUser() {
        Avtal saved1 = buildAvtal(1, AVTAL_TEXT);
        Avtal saved2 = buildAvtal(2, AVTAL_TEXT);
        Avtal saved3 = buildAvtal(3, AVTAL_TEXT);
        avtalRepository.save(saved1);
        avtalRepository.save(saved2);
        avtalRepository.save(saved3);

        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        godkantAvtalRepository.approveAvtal(HSA_ID, latestAvtalVersion);

        godkantAvtalRepository.removeAllUserApprovments(HSA_ID);

        boolean approved = godkantAvtalRepository.userHasApprovedAvtal(HSA_ID, latestAvtalVersion);
        assertFalse(approved);
    }

    private Avtal buildAvtal(int version, String avtalText) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(version);
        avtal.setAvtalText(avtalText);
        avtal.setVersionDatum(LocalDateTime.now());
        return avtal;
    }

}
