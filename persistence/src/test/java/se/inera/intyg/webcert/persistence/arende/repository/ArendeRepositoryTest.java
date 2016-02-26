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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.model.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class ArendeRepositoryTest {

    @Autowired
    private ArendeRepository repo;

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
        assertEquals(read.getEnhet(), saved.getEnhet());
        assertEquals(read.getStatus(), saved.getStatus());
        assertEquals(read.getTimestamp(), saved.getTimestamp());

        assertEquals(read.getKomplettering(), saved.getKomplettering());
        assertEquals(read.getKontaktInfo(), saved.getKontaktInfo());
    }

    @Test
    public void testFindSigneratAvByEnhet() {
        final String signeratAv1 = "signerat av 1";
        final String signeratAv2 = "signerat av 2";
        final List<String> signeratAv = Arrays.asList(signeratAv1, signeratAv2);
        final String enhet = "enhet";
        repo.save(buildArende(signeratAv1, enhet));
        repo.save(buildArende(signeratAv2, enhet));

        List<String> result = repo.findSigneratAvByEnhet(Arrays.asList(enhet));
        assertEquals(signeratAv, result);
    }

    @Test
    public void testFindSigneratAvByEnhetNoMatch() {
        final String signeratAv1 = "signerat av 1";
        final String signeratAv2 = "signerat av 2";
        final String enhet = "enhet";
        repo.save(buildArende(signeratAv1, enhet));
        repo.save(buildArende(signeratAv2, enhet));

        List<String> result = repo.findSigneratAvByEnhet(Arrays.asList("annan enhet"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindSigneratAvByEnhetMultipleUnits() {
        final String signeratAv1 = "signerat av 1";
        final String signeratAv2 = "signerat av 2";
        final List<String> signeratAv = Arrays.asList(signeratAv1, signeratAv2);
        final String enhet1 = "enhet 1";
        final String enhet2 = "enhet 2";
        repo.save(buildArende(signeratAv1, enhet1));
        repo.save(buildArende(signeratAv2, enhet2));

        List<String> result = repo.findSigneratAvByEnhet(Arrays.asList(enhet1, enhet2, "annan enhet"));
        assertEquals(signeratAv, result);
    }

    @Test
    public void testFindSigneratAvByEnhetNull() {
        final String signeratAv1 = "signerat av 1";
        final String signeratAv2 = "signerat av 2";
        final String enhet = "enhet";
        repo.save(buildArende(signeratAv1, enhet));
        repo.save(buildArende(signeratAv2, enhet));

        List<String> result = repo.findSigneratAvByEnhet(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByEnhet() {
        final String enhet = "enhet";
        repo.save(buildArende("signerat", enhet, Status.PENDING_INTERNAL_ACTION));
        repo.save(buildArende("signerat", enhet, Status.CLOSED));
        repo.save(buildArende("signerat", "annan enhet", Status.PENDING_INTERNAL_ACTION));

        List<Arende> result = repo.findByEnhet(Arrays.asList(enhet));
        assertEquals(1, result.size());
        assertEquals(enhet, result.get(0).getEnhet());
    }

    private Arende buildArende() {
        return buildArende("SIGNERAT_AV", "ENHET");
    }

    private Arende buildArende(String signeratAv, String enhet) {
        return buildArende(signeratAv, enhet, Status.PENDING_INTERNAL_ACTION);
    }

    private Arende buildArende(String signeratAv, String enhet, Status status) {
        Arende res = new Arende();
        res.setAmne(ArendeAmne.KONTKT);
        res.setIntygsId("INTYG_ID");
        res.setMeddelande("MEDDELANDE");
        res.setMeddelandeId("MEDDELANDE_ID");
        res.setPaminnelseMeddelandeId("PAMINNELSE_MEDDELANDE_ID");
        res.setPatientPersonId("PATIENT_PERSON_ID");
        res.setReferensId("REFERENS_ID");
        res.setRubrik("RUBRIK");
        res.setSistaDatumForSvar(LocalDate.now().plusDays(3));
        res.setSkickatAv("SKICKAT_AV");
        res.setSkickatTidpunkt(LocalDateTime.now().minusDays(3));
        res.setSvarPaId("SVAR_PA_ID");
        res.setSvarPaReferens("SVAR_PA_REFERENS");
        res.setIntygTyp("INTYG_TYP");
        res.setSigneratAv(signeratAv);
        res.setEnhet(enhet);
        res.setStatus(status);
        res.setTimestamp(LocalDateTime.now());

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
