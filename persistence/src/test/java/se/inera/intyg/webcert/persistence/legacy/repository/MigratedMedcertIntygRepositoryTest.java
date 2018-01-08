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
package se.inera.intyg.webcert.persistence.legacy.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.legacy.model.MigreratMedcertIntyg;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class MigratedMedcertIntygRepositoryTest {

    @Autowired
    private MigreratMedcertIntygRepository medcertIntygRepository;

    @Test
    public void testSaveMigreratIntyg() {

        MigreratMedcertIntyg intyg1 = new MigreratMedcertIntyg();

        intyg1.setIntygsId("intyg1");
        intyg1.setEnhetsId("enhet1");
        intyg1.setIntygsTyp("fk7263");
        intyg1.setMigreradFran("landtinget");
        intyg1.setPatientNamn("Test Testsson");
        intyg1.setPatientPersonnummer(new Personnummer("19121212-1212"));
        intyg1.setSkapad(LocalDateTime.parse("2013-03-01T11:11:11"));
        intyg1.setSkickad(LocalDateTime.parse("2013-03-01T12:34:56"));
        intyg1.setUrsprung("APPLICATION");
        intyg1.setIntygsData("VGhpcyBpcyBhIGxlZ2FjeSBjZXJ0aWZpY2F0ZQ==".getBytes());

        medcertIntygRepository.save(intyg1);

        MigreratMedcertIntyg intyg2 = medcertIntygRepository.findOne("intyg1");
        assertNotNull(intyg2);
        assertEquals("intyg1", intyg2.getIntygsId());
        assertEquals("Test Testsson", intyg2.getPatientNamn());
        assertEquals(LocalDateTime.parse("2013-03-01T12:34:56"), intyg2.getSkickad());
        assertTrue(intyg2.getIntygsData() != null);
    }
}
