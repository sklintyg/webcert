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
package se.inera.intyg.webcert.persistence.arende.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class ArendeDraftRepositoryTest {

    @Autowired
    private ArendeDraftRepository repo;

    @After
    public void cleanup() {
        repo.deleteAll();
    }

    @Test
    public void testFindByIntygId() {
        repo.save(buildArendeDraft("0"));
        repo.save(buildArendeDraft("1"));
        repo.save(buildArendeDraft("1"));
        repo.save(buildArendeDraft("2"));

        List<ArendeDraft> res = repo.findByIntygId("1");

        assertNotNull(res);
        assertEquals(2, res.size());

        res = repo.findByIntygId("-1");

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testFindByIntygIdAndQuestionId() {
        ArendeDraft res = repo.findByIntygIdAndQuestionId("-1", "-1");
        assertNull(res);

        repo.save(buildArendeDraft("i1", "q1"));

        res = repo.findByIntygIdAndQuestionId("i1", "q1");

        assertNotNull(res);
        assertEquals("i1", res.getIntygId());
        assertEquals("q1", res.getQuestionId());

        repo.save(buildArendeDraft("i1"));

        res = repo.findByIntygIdAndQuestionId("i1", null);

        assertNotNull(res);
        assertEquals("i1", res.getIntygId());
        assertNull(res.getQuestionId());
    }

    private ArendeDraft buildArendeDraft(String intygId, String questionId) {
        ArendeDraft arendeDraft = new ArendeDraft();
        arendeDraft.setQuestionId(questionId);
        arendeDraft.setIntygId(intygId);
        return arendeDraft;
    }

    private ArendeDraft buildArendeDraft(String intygId) {
        return buildArendeDraft(intygId, null);
    }
}
