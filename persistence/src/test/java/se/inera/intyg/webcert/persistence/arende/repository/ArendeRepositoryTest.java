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

package se.inera.intyg.webcert.persistence.arende.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.model.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class ArendeRepositoryTest {

    @Autowired
    private ArendeRepository repo;

    @After
    public void cleanup() {
        repo.deleteAll();
    }

    @Test
    public void testFindOne() {
        Arende saved = buildArende();
        repo.save(saved);
        Arende read = repo.findOne(saved.getId());
        assertEquals(read.getId(), saved.getId());
        assertEquals(read.getAmne(), saved.getAmne());
        assertEquals(read.getIntygsId(), saved.getIntygsId());
        assertEquals(read.getMeddelande(), saved.getMeddelande());
        assertEquals(read.getMeddelandeId(), saved.getMeddelandeId());
        assertEquals(read.getPaminnelseMeddelandeId(), saved.getPaminnelseMeddelandeId());
        assertEquals(read.getPatientPersonId(), saved.getPatientPersonId());
        assertEquals(read.getReferensId(), saved.getReferensId());
        assertEquals(read.getRubrik(), saved.getRubrik());
        assertEquals(read.getSistaDatumForSvar(), saved.getSistaDatumForSvar());
        assertEquals(read.getSkickatAv(), saved.getSkickatAv());
        assertEquals(read.getSkickatTidpunkt(), saved.getSkickatTidpunkt());
        assertEquals(read.getSvarPaId(), saved.getSvarPaId());
        assertEquals(read.getSvarPaReferens(), saved.getSvarPaReferens());
        assertEquals(read.getIntygTyp(), saved.getIntygTyp());
        assertEquals(read.getSigneratAv(), saved.getSigneratAv());
        assertEquals(read.getSigneratAvName(), saved.getSigneratAvName());
        assertEquals(read.getEnhet(), saved.getEnhet());
        assertEquals(read.getStatus(), saved.getStatus());
        assertEquals(read.getTimestamp(), saved.getTimestamp());
        assertEquals(read.getVidarebefordrad(), saved.getVidarebefordrad());
        assertEquals(read.getSenasteHandelse(), saved.getSenasteHandelse());
        assertEquals(read.getVardaktorName(), saved.getVardaktorName());

        assertEquals(read.getKomplettering(), saved.getKomplettering());
        assertEquals(read.getKontaktInfo(), saved.getKontaktInfo());
    }

    @Test
    public void testFindOneByMeddelandeId() {
        final String meddelandeId = "meddelande2";
        repo.save(buildArende("meddelande1"));
        repo.save(buildArende(meddelandeId));
        repo.save(buildArende("meddelande3"));

        Arende arende = repo.findOneByMeddelandeId(meddelandeId);
        assertNotNull(arende);
        assertEquals(meddelandeId, arende.getMeddelandeId());

        assertNull(repo.findOneByMeddelandeId("finns_ej"));
    }

    @Test
    public void testFindSigneratAvByEnhet() {
        final String signeratAv1HsaId = "signerat av 1 - hsa id";
        final String signeratAv1Namn = "signerat av 1 - namn";
        final String signeratAv2HsaId = "signerat av 2 - hsa id";
        final String signeratAv2Namn = "signerat av 2 - namn";
        final String[] expected1 = { signeratAv1HsaId, signeratAv1Namn };
        final String[] expected2 = { signeratAv2HsaId, signeratAv2Namn };
        final String enhet = "enhet";
        repo.save(buildArende(signeratAv1HsaId, signeratAv1Namn, enhet));
        repo.save(buildArende(signeratAv2HsaId, signeratAv2Namn, enhet));

        List<Object[]> result = repo.findSigneratAvByEnhet(Arrays.asList(enhet));

        assertEquals(2, result.size());
        assertEquals(expected1[0], result.get(0)[0]);
        assertEquals(expected1[1], result.get(0)[1]);
        assertEquals(expected2[0], result.get(1)[0]);
        assertEquals(expected2[1], result.get(1)[1]);
    }

    @Test
    public void testFindSigneratAvByEnhetNoMatch() {
        final String signeratAv1HsaId = "signerat av 1 - hsa id";
        final String signeratAv1Namn = "signerat av 1 - namn";
        final String signeratAv2HsaId = "signerat av 2 - hsa id";
        final String signeratAv2Namn = "signerat av 2 - namn";
        final String enhet = "enhet";
        repo.save(buildArende(signeratAv1HsaId, signeratAv1Namn, enhet));
        repo.save(buildArende(signeratAv2HsaId, signeratAv2Namn, enhet));

        List<Object[]> result = repo.findSigneratAvByEnhet(Arrays.asList("annan enhet"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindSigneratAvByEnhetMultipleUnits() {
        final String signeratAv1HsaId = "signerat av 1 - hsa id";
        final String signeratAv1Namn = "signerat av 1 - namn";
        final String signeratAv2HsaId = "signerat av 2 - hsa id";
        final String signeratAv2Namn = "signerat av 2 - namn";
        final String[] expected = { signeratAv2HsaId, signeratAv2Namn };
        final String enhet1 = "enhet 1";
        final String enhet2 = "enhet 2";
        repo.save(buildArende(signeratAv1HsaId, signeratAv1Namn, enhet1));
        repo.save(buildArende(signeratAv2HsaId, signeratAv2Namn, enhet2));

        List<Object[]> result = repo.findSigneratAvByEnhet(Arrays.asList(enhet2, "annan enhet"));
        assertEquals(1, result.size());
        assertEquals(expected[0], result.get(0)[0]);
        assertEquals(expected[1], result.get(0)[1]);
    }

    @Test
    public void testFindSigneratAvByEnhetNull() {
        repo.save(buildArende("hsaid", "namn", "enhet"));
        repo.save(buildArende("hsaid", "namn", "enhet"));

        List<Object[]> result = repo.findSigneratAvByEnhet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByEnhet() {
        final String enhet = "enhet";
        repo.save(buildArende(enhet, Status.PENDING_INTERNAL_ACTION));
        repo.save(buildArende(enhet, Status.CLOSED));
        repo.save(buildArende("annan enhet", Status.PENDING_INTERNAL_ACTION));

        List<Arende> result = repo.findByEnhet(Arrays.asList(enhet));
        assertEquals(1, result.size());
        assertEquals(enhet, result.get(0).getEnhet());
    }

    @Test
    public void testFilterArendeByEnhet() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", "annan enhet", Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeIgnoresSvarAndPaminnelse() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, "paminnelseMeddelandeId", null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, "svarPaId", "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));

        List<Arende> result = repo.filterArende(filter);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterArendeQuestionFromFK() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "FK", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setQuestionFromFK(true);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeQuestionFromWC() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "WC", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setQuestionFromWC(true);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeByHsaId() {
        final String enhet = "enhet";
        final String signeratAvHsaId = "hsaid1";
        repo.save(buildArende(signeratAvHsaId, enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setHsaId(signeratAvHsaId);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeVidarebefordradTrue() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.TRUE, LocalDateTime.now(), UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.FALSE, LocalDateTime.now(), UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, null, LocalDateTime.now(), UUID.randomUUID().toString()));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVidarebefordrad(Boolean.TRUE);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeVidarebefordradFalse() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.TRUE, LocalDateTime.now(), UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.FALSE, LocalDateTime.now(), UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, null, LocalDateTime.now(), UUID.randomUUID().toString()));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVidarebefordrad(Boolean.FALSE);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeChangedFrom() {
        final String enhet = "enhet";
        final LocalDateTime changedFrom = LocalDateTime.now();
        final LocalDateTime beforeChangedFrom = changedFrom.minusDays(1);
        final LocalDateTime afterChangedFrom = changedFrom.plusDays(1);
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.TRUE, changedFrom, UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.FALSE, beforeChangedFrom, UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, null, afterChangedFrom, UUID.randomUUID().toString()));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setChangedFrom(changedFrom);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterArendeChangedTo() {
        final String enhet = "enhet";
        final LocalDateTime changedTo = LocalDateTime.now();
        final LocalDateTime beforeChangedTo = changedTo.minusDays(1);
        final LocalDateTime afterChangedTo = changedTo.plusDays(1);
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.TRUE, changedTo, UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, Boolean.FALSE, beforeChangedTo, UUID.randomUUID().toString()));
        repo.save(buildArende("signeratAv", "signeratAvName", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now()
                .minusDays(3), ArendeAmne.OVRIGT, null, afterChangedTo, UUID.randomUUID().toString()));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setChangedTo(changedTo);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeByReplyLatest() {
        final String enhet = "enhet";
        final LocalDate replyLatest = LocalDate.now();
        final LocalDate beforeReplyLatest = replyLatest.minusDays(1);
        final LocalDate afterReplyLatest = replyLatest.plusDays(1);
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", beforeReplyLatest));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", replyLatest));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", afterReplyLatest));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setReplyLatest(replyLatest);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterArendeAllaOhanterade() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.ALLA_OHANTERADE);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(3, result.size());
    }

    @Test
    public void testFilterArendeHanterad() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.HANTERAD);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeKomplettering() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3), ArendeAmne.KOMPLT));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.KOMPLT));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.OVRIGT));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.KOMPLETTERING_FRAN_VARDEN);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeSvarFranVarden() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3), ArendeAmne.ARBTID));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.KOMPLT));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.OVRIGT));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.AVSTMN));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.ARBTID));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.KONTKT));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.SVAR_FRAN_VARDEN);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(4, result.size());
    }

    @Test
    public void testFilterArendeSvarFranFK() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendeMarkeraSomHanterad() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3),
                ArendeAmne.PAMINN));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setVantarPa(VantarPa.MARKERA_SOM_HANTERAD);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterArendePaginated() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setStartFrom(0);
        filter.setPageSize(1);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterArendePaginated2() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));
        filter.setStartFrom(1);
        filter.setPageSize(10);

        List<Arende> result = repo.filterArende(filter);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterArendeCount() {
        final String enhet = "enhet";
        repo.save(buildArende("signeratAv", enhet, Status.CLOSED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.ANSWERED, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_EXTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));
        repo.save(buildArende("signeratAv", enhet, Status.PENDING_INTERNAL_ACTION, null, null, "SKICKAT_AV", LocalDate.now().minusDays(3)));

        Filter filter = new Filter();
        filter.setEnhetsIds(Arrays.asList(enhet));

        int result = repo.filterArendeCount(filter);
        assertEquals(3, result);
    }

    @Test
    public void testFindBySvarPaId() {
        Arende fraga = repo.save(buildArende());
        Arende svar = buildArende();
        svar.setSvarPaId(fraga.getMeddelandeId());
        repo.save(svar);

        List<Arende> result = repo.findBySvarPaId(fraga.getMeddelandeId());
        assertEquals(1, result.size());
        assertEquals(svar.getMeddelandeId(), result.get(0).getMeddelandeId());
    }

    @Test
    public void testFindByPaminnelseMeddelandeId() {
        Arende fraga = repo.save(buildArende());
        Arende paminnelse = buildArende();
        paminnelse.setPaminnelseMeddelandeId(fraga.getMeddelandeId());
        repo.save(paminnelse);

        List<Arende> result = repo.findByPaminnelseMeddelandeId(fraga.getMeddelandeId());
        assertEquals(1, result.size());
        assertEquals(paminnelse.getMeddelandeId(), result.get(0).getMeddelandeId());
    }

    private Arende buildArende() {
        return buildArende("SIGNERAT_AV", "SIGNERAT_AV_NAMN", "ENHET");
    }

    private Arende buildArende(String meddelandeId) {
        return buildArende("SIGNERAT_AV", "SIGNERAT_AV_NAMN", "ENHET", Status.PENDING_INTERNAL_ACTION, meddelandeId);
    }

    private Arende buildArende(String enhet, Status status) {
        return buildArende("HSA_ID", "NAME", enhet, status, UUID.randomUUID().toString());
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet) {
        return buildArende(signeratAv, signeratAvName, enhet, Status.PENDING_INTERNAL_ACTION, UUID.randomUUID().toString());
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String meddelandeId) {
        return buildArende(signeratAv, signeratAvName, enhet, status, "PAMINNELSE_MEDDELANDE_ID", "SVAR_PA_ID", "SKICKAT_AV", LocalDate.now(),
                ArendeAmne.OVRIGT, Boolean.TRUE, LocalDateTime.now(), meddelandeId);
    }

    private Arende buildArende(String signeratAv, String enhet, Status status, String paminnelseMeddelandeId, String svarPaId, String skickatAv,
            LocalDate sistaDatumForSvar) {
        return buildArende(signeratAv, enhet, status, paminnelseMeddelandeId, svarPaId, skickatAv, sistaDatumForSvar, ArendeAmne.KONTKT);
    }

    private Arende buildArende(String signeratAv, String enhet, Status status, String paminnelseMeddelandeId, String svarPaId, String skickatAv,
            LocalDate sistaDatumForSvar, ArendeAmne amne) {
        return buildArende(signeratAv, "signeratAvName", enhet, status, paminnelseMeddelandeId, svarPaId, skickatAv, sistaDatumForSvar, amne,
                Boolean.TRUE, LocalDateTime.now(), UUID.randomUUID().toString());
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String paminnelseMeddelandeId, String svarPaId,
            String skickatAv, LocalDate sistaDatumForSvar, ArendeAmne amne, Boolean vidarebefordrad, LocalDateTime senasteHandelse,
            String meddelandeId) {
        return buildArende(signeratAv, signeratAvName, enhet, status, paminnelseMeddelandeId, svarPaId, skickatAv, sistaDatumForSvar, amne,
                vidarebefordrad, senasteHandelse, meddelandeId, "vardaktorName");
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String paminnelseMeddelandeId, String svarPaId,
            String skickatAv, LocalDate sistaDatumForSvar, ArendeAmne amne, Boolean vidarebefordrad, LocalDateTime senasteHandelse,
            String meddelandeId, String vardaktorName) {
        Arende res = new Arende();
        res.setAmne(amne);
        res.setIntygsId("INTYG_ID");
        res.setMeddelande("MEDDELANDE");
        res.setMeddelandeId(meddelandeId);
        res.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        res.setPatientPersonId("PATIENT_PERSON_ID");
        res.setReferensId("REFERENS_ID");
        res.setRubrik("RUBRIK");
        res.setSistaDatumForSvar(sistaDatumForSvar);
        res.setSkickatAv(skickatAv);
        res.setSkickatTidpunkt(LocalDateTime.now().minusDays(3));
        res.setSvarPaId(svarPaId);
        res.setSvarPaReferens("SVAR_PA_REFERENS");
        res.setIntygTyp("INTYG_TYP");
        res.setSigneratAv(signeratAv);
        res.setSigneratAvName(signeratAvName);
        res.setEnhet(enhet);
        res.setStatus(status);
        res.setTimestamp(LocalDateTime.now());
        res.setVidarebefordrad(vidarebefordrad);
        res.setSenasteHandelse(senasteHandelse);
        res.setVardaktorName(vardaktorName);

        res.getKomplettering().add(buildMedicinsktArende("1", 1, "text 1"));
        res.getKomplettering().add(buildMedicinsktArende("2", null, "text 2"));
        res.getKomplettering().add(buildMedicinsktArende("3", 3, "text 3"));

        res.getKontaktInfo().add("Kontakt 1");
        res.getKontaktInfo().add("Kontakt 2");
        res.getKontaktInfo().add("Kontakt 3");
        return res;
    }

    private MedicinsktArende buildMedicinsktArende(String frageId, Integer instans, String text) {
        MedicinsktArende res = new MedicinsktArende();
        res.setFrageId(frageId);
        res.setInstans(instans);
        res.setText(text);
        return res;
    }
}
