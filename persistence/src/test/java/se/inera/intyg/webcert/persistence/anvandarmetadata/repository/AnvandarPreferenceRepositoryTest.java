/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.anvandarmetadata.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2015-08-05.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class AnvandarPreferenceRepositoryTest {

    private static final String HSA_ID = "hsaId1";

    @Autowired
    private AnvandarPreferenceRepository anvandarMetadataRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFindOne() {
        AnvandarPreference saved = buildAnvandarPreference(HSA_ID, "key1", "value1");
        anvandarMetadataRepository.save(saved);
        AnvandarPreference read = anvandarMetadataRepository.findOne(saved.getInternReferens());
        assertEquals(saved, read);
    }

    @Test
    public void testGetAnvandarPreference() {
        AnvandarPreference saved = buildAnvandarPreference(HSA_ID, "key1", "value1");
        anvandarMetadataRepository.save(saved);
        AnvandarPreference saved2 = buildAnvandarPreference(HSA_ID, "key2", "value2");
        anvandarMetadataRepository.save(saved2);
        AnvandarPreference savedOther = buildAnvandarPreference("other-id", "key3", "value3");
        anvandarMetadataRepository.save(savedOther);

        Map<String, String> anvandarMetadata = anvandarMetadataRepository.getAnvandarPreference(HSA_ID);
        assertEquals(2, anvandarMetadata.size());
        assertEquals("value1", anvandarMetadata.get("key1"));
        assertEquals("value2", anvandarMetadata.get("key2"));
    }

    private AnvandarPreference buildAnvandarPreference(String hsaId, String key, String value) {
        AnvandarPreference am = new AnvandarPreference();
        am.setHsaId(hsaId);
        am.setKey(key);
        am.setValue(value);
        return am;
    }

}
