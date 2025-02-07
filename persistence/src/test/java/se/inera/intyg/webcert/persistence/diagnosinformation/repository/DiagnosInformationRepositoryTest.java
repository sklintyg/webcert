/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.diagnosinformation.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.fmb.model.dto.MaximalSjukskrivningstidDagar;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Beskrivning;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.BeskrivningTyp;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.DiagnosInformation;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.Icd10Kod;
import se.inera.intyg.webcert.persistence.fmb.model.fmb.TypFall;
import se.inera.intyg.webcert.persistence.fmb.repository.DiagnosInformationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:repository-context.xml"})
@ActiveProfiles({"dev", "unit-testing"})
@Transactional
public class DiagnosInformationRepositoryTest {

    private DiagnosInformation diagnosInfoA10B10;

    @Autowired
    private DiagnosInformationRepository repo;

    @After
    public void clean() {
        repo.deleteAll();
    }

    @Before
    public void setup() {
        final String diagnosRubrik = "diagnosRubrik";
        final String forsakringsmedicinskInformation = "forsakringsmedicinskInformation";
        final String symptomPrognosBehandling = "symptomPrognosBehandling";
        final String informationOmRehabilitering = "informationOmRehabilitering";
        final List<Beskrivning> beskrivningList = Arrays.asList(new Beskrivning(BeskrivningTyp.FUNKTIONSNEDSATTNING,
            "beskrivningText", null));

        final List<Icd10Kod> icd10KodList = Arrays
            .asList(createIcd10Item("A10", Arrays.asList(
                    createTypFall(14, "2", "wk"),
                    createTypFall(10, "10", "d"))),
                createIcd10Item("B10", Arrays.asList(
                    createTypFall(9, "9", "d"),
                    createTypFall(61, "2", "mo"))));
        final LocalDateTime senastUppdaterad = LocalDateTime.now();

        DiagnosInformation.DiagnosInformationBuilder diagnosInformationBuilder = DiagnosInformation.DiagnosInformationBuilder
            .aDiagnosInformation()
            .diagnosRubrik(diagnosRubrik)
            .forsakringsmedicinskInformation(forsakringsmedicinskInformation)
            .symptomPrognosBehandling(symptomPrognosBehandling)
            .informationOmRehabilitering(informationOmRehabilitering)
            .beskrivningList(beskrivningList)
            .icd10KodList(icd10KodList)
            .senastUppdaterad(senastUppdaterad);

        diagnosInfoA10B10 = repo.save(diagnosInformationBuilder.build());
    }

    private TypFall createTypFall(int maxRekDagar, String sourceValue, String sourceUnit) {
        return TypFall.TypFallBuilder.aTypFall()
            .typfallsMening("Beskrivning " + maxRekDagar)
            .maximalSjukrivningstidDagar(maxRekDagar)
            .maximalSjukrivningstidSourceValue(sourceValue)
            .maximalSjukrivningstidSourceUnit(sourceUnit)
            .build();

    }

    private Icd10Kod createIcd10Item(String kod, List<TypFall> typfallsList) {
        return Icd10Kod.Icd10KodBuilder.anIcd10Kod().kod(kod).typFallList(typfallsList).build();
    }

    @Test
    public void testFindFirstByIcd10KodListkod() {
        DiagnosInformation read = repo.findFirstByIcd10KodList_kod("A10").get();
        assertEquals(diagnosInfoA10B10, read);
    }

    @Test
    public void testFindFirstByIcd10KodListkodNotFound() {
        assertFalse(repo.findFirstByIcd10KodList_kod("A11").isPresent());
    }

    @Test
    public void testFindMaximalSjukrivningstidDagarByIcd10KoderA10() {

        Set<String> diagnoser = new HashSet<>();
        diagnoser.add("A10");

        final List<MaximalSjukskrivningstidDagar> result = repo.findMaximalSjukrivningstidDagarByIcd10Koder(diagnoser);
        assertEquals(2, result.size());
        assertEquals("A10", result.get(0).getIcd10Kod());
        assertEquals(14, result.get(0).getMaximalSjukrivningstidDagar());
        assertEquals("2", result.get(0).getMaximalSjukrivningstidSourceValue());
        assertEquals("wk", result.get(0).getMaximalSjukrivningstidSourceUnit());

        assertEquals("A10", result.get(1).getIcd10Kod());
        assertEquals(10, result.get(1).getMaximalSjukrivningstidDagar());
        assertEquals("10", result.get(1).getMaximalSjukrivningstidSourceValue());
        assertEquals("d", result.get(1).getMaximalSjukrivningstidSourceUnit());
    }

    @Test
    public void testFindMaximalSjukrivningstidDagarByIcd10KoderA10B10() {

        Set<String> diagnoser = new HashSet<>();
        diagnoser.add("A10");
        diagnoser.add("B10");

        final List<MaximalSjukskrivningstidDagar> result = repo.findMaximalSjukrivningstidDagarByIcd10Koder(diagnoser);
        assertEquals(4, result.size());

        assertEquals("B10", result.get(0).getIcd10Kod());
        assertEquals(61, result.get(0).getMaximalSjukrivningstidDagar());
        assertEquals("2", result.get(0).getMaximalSjukrivningstidSourceValue());
        assertEquals("mo", result.get(0).getMaximalSjukrivningstidSourceUnit());

        assertEquals("A10", result.get(1).getIcd10Kod());
        assertEquals(14, result.get(1).getMaximalSjukrivningstidDagar());
        assertEquals("2", result.get(1).getMaximalSjukrivningstidSourceValue());
        assertEquals("wk", result.get(1).getMaximalSjukrivningstidSourceUnit());

        assertEquals("A10", result.get(2).getIcd10Kod());
        assertEquals(10, result.get(2).getMaximalSjukrivningstidDagar());
        assertEquals("10", result.get(2).getMaximalSjukrivningstidSourceValue());
        assertEquals("d", result.get(2).getMaximalSjukrivningstidSourceUnit());

        assertEquals("B10", result.get(3).getIcd10Kod());
        assertEquals(9, result.get(3).getMaximalSjukrivningstidDagar());
        assertEquals("9", result.get(3).getMaximalSjukrivningstidSourceValue());
        assertEquals("d", result.get(3).getMaximalSjukrivningstidSourceUnit());
    }
}
