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
package se.inera.intyg.webcert.web.service.diagnos.repo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosRepositoryFactoryTest-context.xml")
public class DiagnosRepositoryTest {

    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";

    @Autowired
    private DiagnosRepositoryFactory factory;

    private DiagnosRepository repo;

    @Before
    public void setup() {
        DiagnosRepositoryImpl repoImpl = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository(Collections.singletonList(FILE_1));
        assertEquals(100, repoImpl.nbrOfDiagosis());
        this.repo = repoImpl;
    }

    @Test
    public void testSanitizeCodeValue() {
        DiagnosRepositoryImpl repoImpl = new DiagnosRepositoryImpl();
        assertNull("null should return null", repoImpl.sanitizeCodeValue(null));
        assertNull("emptry string should return null", repoImpl.sanitizeCodeValue(""));
        assertNull("spaces should return null", repoImpl.sanitizeCodeValue("  "));
        assertNull(". should return null", repoImpl.sanitizeCodeValue("."));
        assertNull(". and spaces should return null", repoImpl.sanitizeCodeValue(". "));
        assertEquals("A", repoImpl.sanitizeCodeValue("A"));
        assertEquals("A", repoImpl.sanitizeCodeValue("a"));
        assertEquals("A", repoImpl.sanitizeCodeValue(" A "));
        assertEquals("A1", repoImpl.sanitizeCodeValue("A1"));
        assertEquals("A1", repoImpl.sanitizeCodeValue(" A1 "));
        assertEquals("A1", repoImpl.sanitizeCodeValue(" A 1 "));
        assertEquals("A1", repoImpl.sanitizeCodeValue("A.1"));
        assertEquals("A11", repoImpl.sanitizeCodeValue("A1.1"));
        assertEquals("A11", repoImpl.sanitizeCodeValue(" A1 1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue(" A11.1 "));
        assertEquals("A111", repoImpl.sanitizeCodeValue("a11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A11.1"));
        assertEquals("A111", repoImpl.sanitizeCodeValue("A 11.1"));
    }

    @Test
    public void testGetByCodeWithNullAndEmpty() {
        List<Diagnos> res = repo.getDiagnosesByCode(null);
        assertTrue(res.isEmpty());

        res = repo.getDiagnosesByCode("");
        assertTrue(res.isEmpty());
    }

    @Test
    public void testGetByCodeFour() {
        String code = "A184";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    public void testGetByCodeFourAndDot() {
        String code = "A18.4";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    public void testGetByCodeFive() {
        String code = "A184E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    public void testGetByCodeFiveAndDot() {
        String code = "A18.4E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.get(0).getKod());
        assertThat(res.get(0).getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    public void testGetByCodeWithMalformedCode() {
        String code = " a 051  ";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertNotNull(res);
        assertEquals("A051", res.get(0).getKod());
    }

    @Test
    public void testCodeSearchWithFragmentThree() {
        String codeFragment = "A08";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(9, res.size());
    }

    @Test
    public void testCodeSearchWithFragmentFour() {
        String codeFragment = "A083";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    public void testCodeSearchWithFragmentFourAndDot() {
        String codeFragment = "A08.3";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    public void testCodeSearchWithFullCode() {
        String codeFragment = "A083B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    public void testCodeSearchWithFullCodeAndDot() {
        String codeFragment = "A08.3B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    public void testCodeSearchWithNonExistingFragment() {
        String codeFragment = "X01";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testCodeSearchWithNoInput() {
        String codeFragment = "";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testCodeSearchWithNull() {
        final String codeFragment = null;
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testDescriptionSearchWithNoInput() {
        List<Diagnos> res = repo.searchDiagnosisByDescription("", 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testDescriptionSearchWithNull() {
        // Given
        String searchTerm = null;
        int greaterThanZero = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanZero);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                is(empty())));
    }

    @Test
    public void testDescriptionSearchWithNonExistentTerm() {
        // Given
        String searchTerm = "foobar";
        int greaterThanZero = 100;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanZero);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                is(empty())));
    }

    @Test
    public void testDescriptionSearchWithMatchAtBeginningOfDescription() {
        // Given
        String searchTerm = "Botuli";
        int greaterThanZero = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanZero);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", startsWith(searchTerm)))));
    }

    @Test
    public void testDescriptionSearchWithMatchInTheMiddleOfDescription() {
        // Given
        String searchTerm = "sputumundersökning";
        int greaterThanZero = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanZero);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    public void testDescriptionSearchWithPartialWord() {
        // Given
        String searchTerm = "tumundersökni";
        int greaterThanZero = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanZero);

        // Then
        String expectedResult = "sputumundersökning";
        assertThat(result, allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", containsString(expectedResult)))));
    }

    @Test
    public void testDescriptionSearchWithMultipleResults() {
        // Given
        String searchTerm = "Paratyfoidfebe";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(4),
                everyItem(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    public void testDescriptionSearchWithMultipleWordSearchTerm() {
        // Given
        String searchTerm = "Tuberkulos i intratorakala lymfkörtlar, utan uppgift om bakteriologisk eller histologisk verifikation";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    public void testDescriptionSearchWithMultipleTruncatedWordSearchTerm() {
        // Given
        String searchTerm = "Tuber intrato lymfkö, utan uppg om bakteri eller histol verifi";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        String expectedResult = "Tuberkulos i intratorakala lymfkörtlar, utan uppgift om bakteriologisk eller histologisk verifikation";
        assertThat(result, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", containsString(expectedResult)))));
    }

    @Test
    public void testDescriptionSearchWithMultiplePartialWordSearchTerm() {
        // Given
        String searchTerm = "berku i ntratora ymfkörtla, tan ppgi m riologisk eller histolo ifikatio";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        String expectedResult = "Tuberkulos i intratorakala lymfkörtlar, utan uppgift om bakteriologisk eller histologisk verifikation";
        assertThat(result, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(1),
                contains(hasProperty("beskrivning", containsString(expectedResult)))));
    }

    @Test
    public void testDescriptionSearchWithMultipleResultsCanReturnLess() {
        // Given
        String searchTerm = "Paratyfoidfebe";
        int oneLessThanAvailableResults = 2;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, oneLessThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(oneLessThanAvailableResults),
                everyItem(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    public void testDescriptionSearchIgnoresCase() {
        // Given
        String searchTerm = "Lungtuberkulos";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> mixedCaseResults = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);
        List<Diagnos> upperCaseResults = repo.searchDiagnosisByDescription(searchTerm.toUpperCase(), greaterThanAvailableResults);
        List<Diagnos> lowerCaseResults = repo.searchDiagnosisByDescription(searchTerm.toLowerCase(), greaterThanAvailableResults);

        // Then
        assertThat(mixedCaseResults, Matchers.<List<Diagnos>> allOf(
                is(notNullValue()),
                hasSize(7),
                everyItem(hasProperty("beskrivning", containsString(searchTerm))),
                contains(upperCaseResults.toArray()), // Converting to array needed, to match correct method signature
                contains(lowerCaseResults.toArray())));
    }

}
