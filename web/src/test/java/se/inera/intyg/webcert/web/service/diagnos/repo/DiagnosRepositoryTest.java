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
package se.inera.intyg.webcert.web.service.diagnos.repo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosRepositoryFactoryTest-context.xml")
class DiagnosRepositoryTest {

    private static final String FILE = "classpath:DiagnosService/icd10se/icd-10-se.tsv";

    @Autowired
    private DiagnosRepositoryFactory factory;

    private DiagnosRepository repo;

    @BeforeEach
    public void setup() {
        List<String> fileList = List.of(FILE);
        DiagnosRepositoryImpl repoImpl = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository(fileList,
            StandardCharsets.UTF_8);
        assertEquals(38628, repoImpl.nbrOfDiagosis());
        this.repo = repoImpl;
    }

    @Test
    void testSanitizeCodeValue() {
        DiagnosRepositoryImpl repoImpl = new DiagnosRepositoryImpl();
        assertNull(repoImpl.sanitizeCodeValue(null), "null should return null");
        assertNull(repoImpl.sanitizeCodeValue(""), "emptry string should return null");
        assertNull(repoImpl.sanitizeCodeValue("  "), "spaces should return null");
        assertNull(repoImpl.sanitizeCodeValue("."), ". should return null");
        assertNull(repoImpl.sanitizeCodeValue(". "), ". and spaces should return null");
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
    void testGetByCodeWithNullAndEmpty() {
        List<Diagnos> res = repo.getDiagnosesByCode(null);
        assertTrue(res.isEmpty());

        res = repo.getDiagnosesByCode("");
        assertTrue(res.isEmpty());
    }

    @Test
    void testGetByCodeFour() {
        String code = "A184";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.getFirst().getKod());
        assertThat(res.getFirst().getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    void testGetByCodeFourAndDot() {
        String code = "A18.4";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184", res.getFirst().getKod());
        assertThat(res.getFirst().getBeskrivning(), containsString("Tuberkulos"));
    }

    @Test
    void testGetByCodeFive() {
        String code = "A184E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.getFirst().getKod());
        assertThat(res.getFirst().getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    void testGetByCodeFiveAndDot() {
        String code = "A18.4E";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertEquals("A184E", res.getFirst().getKod());
        assertThat(res.getFirst().getBeskrivning(), containsString("Tuberkulöst"));
    }

    @Test
    void testGetByCodeWithMalformedCode() {
        String code = " c 76  ";
        List<Diagnos> res = repo.getDiagnosesByCode(code);
        assertEquals(1, res.size());
        assertNotNull(res);
        assertEquals("C76", res.getFirst().getKod());
    }

    @Test
    void testCodeSearchWithFragmentThree() {
        String codeFragment = "A08";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(10, res.size());
    }

    @Test
    void testCodeSearchWithFragmentFour() {
        String codeFragment = "A083";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    void testCodeSearchWithFragmentFourAndDot() {
        String codeFragment = "A08.3";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(4, res.size());
    }

    @Test
    void testCodeSearchWithFullCode() {
        String codeFragment = "A083B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    void testCodeSearchWithFullCodeAndDot() {
        String codeFragment = "A08.3B";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertEquals(1, res.size());
    }

    @Test
    void testCodeSearchWithNonExistingFragment() {
        String codeFragment = "C99";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testCodeSearchWithNoInput() {
        String codeFragment = "";
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testCodeSearchWithNull() {
        final String codeFragment = null;
        List<Diagnos> res = repo.searchDiagnosisByCode(codeFragment, 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testDescriptionSearchWithNoInput() {
        List<Diagnos> res = repo.searchDiagnosisByDescription("", 100);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testDescriptionSearchWithNull() {
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
    void testDescriptionSearchWithNonExistentTerm() {
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
    void testDescriptionSearchWithMatchAtBeginningOfDescription() {
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
    void testDescriptionSearchWithMatchInTheMiddleOfDescription() {
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
    void testDescriptionSearchWithPartialWord() {
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
    void testDescriptionSearchWithMultipleResults() {
        // Given
        String searchTerm = "Paratyfoidfebe";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(8),
            everyItem(hasProperty("beskrivning", containsStringIgnoringCase(searchTerm)))));
    }

    @Test
    void testDescriptionSearchWithMultipleWordSearchTerm() {
        // Given
        String searchTerm = "Tuberkulos i andningsorganen, ej verifierad bakteriologiskt eller histologiskt";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(1),
            contains(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    void testDescriptionSearchWithMultipleTruncatedWordSearchTerm() {
        // Given
        String searchTerm = "Tuber i andningsor, ej verifie bakteriologi eller histologi";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        String expectedResult = "Tuberkulos i andningsorganen, ej verifierad bakteriologiskt eller histologiskt";
        assertThat(result, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(1),
            contains(hasProperty("beskrivning", containsString(expectedResult)))));
    }

    @Test
    void testDescriptionSearchWithMultiplePartialWordSearchTerm() {
        // Given
        String searchTerm = "ber i ndningsor, j rifie teriologi eller histologi";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);

        // Then
        String expectedResult = "Tuberkulos i andningsorganen, ej verifierad bakteriologiskt eller histologiskt";
        assertThat(result, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(1),
            contains(hasProperty("beskrivning", containsString(expectedResult)))));
    }

    @Test
    void testDescriptionSearchWithMultipleResultsCanReturnLess() {
        // Given
        String searchTerm = "Sekundär malign tumör";
        int oneLessThanAvailableResults = 2;

        // When
        List<Diagnos> result = repo.searchDiagnosisByDescription(searchTerm, oneLessThanAvailableResults);

        // Then
        assertThat(result, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(oneLessThanAvailableResults),
            everyItem(hasProperty("beskrivning", containsString(searchTerm)))));
    }

    @Test
    void testDescriptionSearchIgnoresCase() {
        // Given
        String searchTerm = "Lungtuberkulos";
        int greaterThanAvailableResults = Integer.MAX_VALUE;

        // When
        List<Diagnos> mixedCaseResults = repo.searchDiagnosisByDescription(searchTerm, greaterThanAvailableResults);
        List<Diagnos> upperCaseResults = repo.searchDiagnosisByDescription(searchTerm.toUpperCase(), greaterThanAvailableResults);
        List<Diagnos> lowerCaseResults = repo.searchDiagnosisByDescription(searchTerm.toLowerCase(), greaterThanAvailableResults);

        // Then
        assertThat(mixedCaseResults, Matchers.<List<Diagnos>>allOf(
            is(notNullValue()),
            hasSize(8),
            everyItem(hasProperty("beskrivning", containsStringIgnoringCase(searchTerm))),
            contains(upperCaseResults.toArray()), // Converting to array needed, to match correct method signature
            contains(lowerCaseResults.toArray())));
    }

}
