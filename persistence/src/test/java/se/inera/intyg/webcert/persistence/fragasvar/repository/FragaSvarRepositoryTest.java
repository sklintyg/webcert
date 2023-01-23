/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles({"dev", "unit-testing"})
@Transactional
public class FragaSvarRepositoryTest {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @PersistenceContext
    private EntityManager em;

    private static final String INTYGS_ID = "abc123";

    private LocalDateTime FRAGA_SIGN_DATE = LocalDateTime.parse("2013-03-01T11:11:11");
    private LocalDateTime FRAGA_SENT_DATE = LocalDateTime.parse("2013-03-01T12:00:00");
    private LocalDateTime SVAR_SIGN_DATE = LocalDateTime.parse("2013-04-01T11:11:11");
    private LocalDateTime SVAR_SENT_DATE = LocalDateTime.parse("2013-04-01T12:00:00");

    private IntygsReferens INTYGS_REFERENS = new IntygsReferens(INTYGS_ID, "fk7263",
        Objects.requireNonNull(Personnummer.createPersonnummer("19121212-1212").orElse(null)), "Sven Persson", FRAGA_SENT_DATE);

    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

    private static String HSA_1_ID = "HSA_1_ID";
    private static String HSA_2_ID = "HSA_2_ID";
    private static String HSA_3_ID = "HSA_3_ID";
    private static String HSA_4_ID = "HSA_4_ID";

    private static String HSA_1_NAMN = "A HSA NAMN 1";
    private static String HSA_2_NAMN = "B HSA NAMN 2";
    private static String HSA_3_NAMN = "C HSA NAMN 3";
    private static String HSA_4_NAMN = "D HSA NAMN 4";

    @Test
    public void testFindOne() {
        FragaSvar saved = buildFragaSvarFraga(ENHET_1_ID);
        fragasvarRepository.save(saved);
        FragaSvar read = fragasvarRepository.findById(saved.getInternReferens()).orElse(null);
        assertNotNull(read);
        assertEquals(read.getInternReferens(), saved.getInternReferens());
        assertEquals(read.getAmne(), saved.getAmne());
        assertEquals(read.getExternReferens(), saved.getExternReferens());
        assertEquals(read.getFrageSigneringsDatum(), saved.getFrageSigneringsDatum());
        assertEquals(read.getFrageSkickadDatum(), saved.getFrageSkickadDatum());
        assertEquals(read.getFrageStallare(), saved.getFrageStallare());
        assertEquals(read.getFrageText(), saved.getFrageText());
        assertEquals(read.getIntygsReferens(), saved.getIntygsReferens());


    }

    @Test
    public void testFindByEnhetsId() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_4_ID));

        List<FragaSvar> result = fragasvarRepository.findByEnhetsId(Arrays.asList(ENHET_1_ID, ENHET_3_ID));
        assertEquals(3, result.size());

    }

    @Test
    public void testFindByEnhetsIdDontMatchClosed() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        List<FragaSvar> result = fragasvarRepository.findByEnhetsId(Arrays.asList(ENHET_1_ID, ENHET_2_ID, ENHET_3_ID));
        assertEquals(3, result.size());

    }

    @Test
    public void testcountUnhandledForEnhetsIds() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        long result = fragasvarRepository.countUnhandledForEnhetsIds(Arrays.asList(ENHET_1_ID, ENHET_2_ID));
        assertEquals(3, result);

    }

    @Test
    public void testCountUnhandledByEnhet() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));                     // NONE BELOW HERE
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        List<GroupableItem> res = fragasvarRepository
            .getUnhandledWithEnhetIdsAndIntygstyper(Arrays.asList(ENHET_1_ID, ENHET_2_ID), set("fk7263"));
        assertNotNull(res);
        assertEquals(3, res.size());
    }

    @Test
    public void testCountUnhandledByEnhetAndIntygsTyper() {

        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.CLOSED));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.CLOSED));

        // With valid type
        List<GroupableItem> res = fragasvarRepository
            .getUnhandledWithEnhetIdsAndIntygstyper(Arrays.asList(ENHET_1_ID, ENHET_2_ID), set("fk7263"));
        assertNotNull(res);
        assertEquals(3, res.size());

        // With unknown type
        res = fragasvarRepository.getUnhandledWithEnhetIdsAndIntygstyper(Arrays.asList(ENHET_1_ID, ENHET_2_ID), set("other-type"));
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testFindByIntygsReferens() {
        FragaSvar saved = buildFragaSvarFraga(ENHET_1_ID);
        saved.setIntygsReferens(new IntygsReferens("non-existing-intygs-id", "fk",
            Objects.requireNonNull(Personnummer.createPersonnummer("19121212-1212").orElse(null)), "Sven Persson", FRAGA_SENT_DATE));
        fragasvarRepository.save(saved);
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_4_ID));

        List<FragaSvar> result = fragasvarRepository.findByIntygsReferensIntygsId(INTYGS_REFERENS.getIntygsId());
        assertEquals(2, result.size());

    }

    private FragaSvar buildFragaSvarFraga(String enhetsId) {
        return buildFragaSvarFraga(enhetsId, Status.PENDING_EXTERNAL_ACTION);
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status) {
        return buildFragaSvarFraga(enhetsId, status, false);
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, boolean answered) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare("Olle");
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        if (answered) {
            f.setSvarsText("Ett svar på frågan");
        }

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String hsaid, String hsaNamn) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare("Olle");
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        vardperson.setHsaId(hsaid);
        vardperson.setNamn(hsaNamn);
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String frageStallare) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare(frageStallare);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        return f;
    }

    private FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String frageStallare, boolean answered) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.AVSTAMNINGSMOTE);
        f.setExternReferens("externReferens");
        f.setFrageSigneringsDatum(FRAGA_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGA_SENT_DATE);
        f.setFrageStallare(frageStallare);
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        if (answered) {
            f.setSvarsText("Ett svar på frågan");
        }

        return f;
    }

    @Test
    public void testFindByExternReferens() {
        FragaSvar saved = buildFragaSvarFraga("Enhet-1-id", Status.PENDING_EXTERNAL_ACTION);

        fragasvarRepository.save(saved);

        FragaSvar read = fragasvarRepository.findByExternReferens(saved.getExternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());

    }

    @Test
    public void testFragaSenasteHandelse() {
        FragaSvar saved = buildFragaSvarFraga("Enhet-1-id", Status.PENDING_EXTERNAL_ACTION);

        fragasvarRepository.save(saved);

        FragaSvar read = fragasvarRepository.findByExternReferens(saved.getExternReferens());

        assertEquals(read.getFrageSkickadDatum(), read.getSenasteHandelse());

        read.setSvarsText("svarstext");
        read.setSvarSkickadDatum(SVAR_SENT_DATE);
        read.setSvarSigneringsDatum(SVAR_SIGN_DATE);

        FragaSvar svar2 = fragasvarRepository.save(read);

        FragaSvar read2 = fragasvarRepository.findByExternReferens(svar2.getExternReferens());

        assertEquals(read2.getSvarSkickadDatum(), read2.getSenasteHandelse());

    }

    @Test
    public void testFindAllHSAIDByEnhet() {
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_1_ID, HSA_1_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_3_ID, HSA_3_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_3_ID, Status.PENDING_INTERNAL_ACTION, HSA_4_ID, HSA_4_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_2_ID, Status.PENDING_INTERNAL_ACTION, HSA_2_ID, HSA_2_NAMN));

        List<String> params = Arrays.asList(ENHET_1_ID, ENHET_2_ID);

        List<Object[]> lakare = fragasvarRepository.findDistinctFragaSvarHsaIdByEnhet(params);

        // Assert that we only get 3 items back.
        assertEquals(3, lakare.size());

        // Assert that no value is HSA_4_ID. Wrong Enhet
        for (int i = 0; i < lakare.size(); i++) {
            assertNotEquals(lakare.get(i)[0], HSA_4_ID);
        }

        // Results should be sorted by name, so we should always get them in the same order.
        assertEquals(lakare.get(0)[0], HSA_1_ID);
        assertEquals(lakare.get(1)[0], HSA_2_ID);
        assertEquals(lakare.get(2)[0], HSA_3_ID);
    }

    @Test
    public void testFilterFragaSvarMatchesOnIntygsTyp() {
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_1_ID, HSA_1_NAMN));

        Filter filter = new Filter();
        filter.getIntygsTyper().add("fk7263");
        filter.getEnhetsIds().add(ENHET_1_ID);
        List<FragaSvar> fragaSvar = fragasvarRepository.filterFragaSvar(filter);
        assertEquals(1, fragaSvar.size());
    }

    @Test
    public void testFilterFragaSvarFiltersOutByIntygsTyp() {
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_1_ID, HSA_1_NAMN));

        Filter filter = new Filter();
        filter.getIntygsTyper().add("annan-typ");
        filter.getEnhetsIds().add(ENHET_1_ID);
        List<FragaSvar> fragaSvar = fragasvarRepository.filterFragaSvar(filter);

        assertEquals(0, fragaSvar.size());
    }

    @Test
    public void testFilterFragaSvarFiltersOutNoIntygsTyp() {
        fragasvarRepository.save(buildFragaSvarFraga(ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, HSA_1_ID, HSA_1_NAMN));

        Filter filter = new Filter();
        filter.getEnhetsIds().add(ENHET_1_ID);
        List<FragaSvar> fragaSvar = fragasvarRepository.filterFragaSvar(filter);

        assertEquals(0, fragaSvar.size());
    }

    private Set<String> set(String... vals) {
        return Stream.of(vals).collect(Collectors.toSet());
    }
}
