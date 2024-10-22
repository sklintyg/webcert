/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles({"dev", "unit-testing"})
@Transactional
public class CertificateEventRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CertificateEventRepository certificateEventRepository;

    @After
    public void cleanup() {
        certificateEventRepository.deleteAll();
    }

    @Test
    public void testFindOneByCertificateId() {
        CertificateEvent savedCertificateEvent = certificateEventRepository
            .save(CertificateEventTestUtil.buildCertificateEvent(CertificateEventTestUtil.CERTIFICATE_ID_1));
        List<CertificateEvent> foundCertificateEvent = certificateEventRepository
            .findByCertificateId(savedCertificateEvent.getCertificateId());

        assertFalse(foundCertificateEvent.isEmpty());
        assertEquals(1, foundCertificateEvent.size());
        assertEquals(savedCertificateEvent, foundCertificateEvent.get(0));
    }

    @Test
    public void testFindAllByCertificateId() {
        certificateEventRepository
            .save(
                CertificateEventTestUtil
                    .buildCertificateEvent(CertificateEventTestUtil.CERTIFICATE_ID_1, CertificateEventTestUtil.EVENT_CODE_SKAPAT));
        certificateEventRepository
            .save(
                CertificateEventTestUtil
                    .buildCertificateEvent(CertificateEventTestUtil.CERTIFICATE_ID_1, CertificateEventTestUtil.EVENT_CODE_SIGNAT));
        certificateEventRepository
            .save(
                CertificateEventTestUtil
                    .buildCertificateEvent(CertificateEventTestUtil.CERTIFICATE_ID_2, CertificateEventTestUtil.EVENT_CODE_SKAPAT));

        List<CertificateEvent> foundCertificateEvent1 = certificateEventRepository
            .findByCertificateId(CertificateEventTestUtil.CERTIFICATE_ID_1);
        List<CertificateEvent> foundCertificateEvent2 = certificateEventRepository
            .findByCertificateId(CertificateEventTestUtil.CERTIFICATE_ID_2);
        assertFalse(foundCertificateEvent2.isEmpty());
        assertEquals(1, foundCertificateEvent2.size());

    }

    @Test
    public void testDeleteCertificateEventByCertificateId() {
        CertificateEvent event1 = certificateEventRepository
            .save(CertificateEventTestUtil.buildCertificateEvent(CertificateEventTestUtil.CERTIFICATE_ID_1));

        certificateEventRepository.delete(event1);
        List<CertificateEvent> found = certificateEventRepository.findByCertificateId(CertificateEventTestUtil.CERTIFICATE_ID_1);
        assertTrue(found.isEmpty());
    }

}
