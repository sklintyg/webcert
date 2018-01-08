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
package se.inera.intyg.webcert.persistence.integreradenhet.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class IntegreradEnhetRepositoryTest {

    @Autowired
    private IntegreradEnhetRepository repository;

    @Test
    public void testSaveIntegreradEnhet() {

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setEnhetsId("SE1234567890-1A01");
        enhet.setEnhetsNamn("Enhet 1");
        enhet.setVardgivarId("SE1234567890-2B01");
        enhet.setVardgivarNamn("Vardgivare 1");
        enhet.setSchemaVersion1(true);
        enhet.setSchemaVersion3(false);

        IntegreradEnhet savedEnhet = repository.save(enhet);

        assertNotNull(savedEnhet);
        assertNotNull(savedEnhet.getSkapadDatum());
        assertNull(savedEnhet.getSenasteKontrollDatum());
        assertTrue(savedEnhet.isSchemaVersion1());
        assertFalse(savedEnhet.isSchemaVersion3());
    }

}
