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

package se.inera.intyg.webcert.web.service.diagnos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosServiceTest-context.xml")
public class DiagnosServiceTest {

    @Autowired
    private DiagnosService service;

    private static final String ICD_10 = Diagnoskodverk.ICD_10_SE.name();

    private static final String KSH97P = Diagnoskodverk.KSH_97_P.name();

    @Test
    public void testGetICD10DiagnosisByCode() {
        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(null, ICD_10).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("", ICD_10).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(" ", ICD_10).getResultat());
    }

    @Test
    public void testGetKSH97PDiagnosisByCode() {
        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(null, KSH97P).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode("", KSH97P).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_CODE, service.getDiagnosisByCode(" ", KSH97P).getResultat());
    }

    @Test
    public void testGetDiagnosisByCodeOrdinaryValue() {
        // Given
        int greaterThanZero = Integer.MAX_VALUE - 1;
        String code = "A04";

        // When
        DiagnosResponse result = service.searchDiagnosisByCode(code, ICD_10, greaterThanZero);

        /*
         * Note: this test, in order to not make assumptions about the underlying implementation, will not assume a
         * hardcoded amount of results to be returned.
         */
        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                is(either(
                        allOf(
                                hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                                hasProperty("diagnoser", hasSize(lessThanOrEqualTo(greaterThanZero)))))
                                        .or(hasProperty("resultat", equalTo(DiagnosResponseType.NOT_FOUND))))));
    }

    @Test
    public void testGetDiagnosisByCodeLimitingAmountOfReturnedResultsIfMoreResultsAreAvailable() {
        // Given
        int arbitraryChosenUpperBound = 2;
        String code = "A04";

        // When
        DiagnosResponse result = service.searchDiagnosisByCode(code, ICD_10, arbitraryChosenUpperBound);
        DiagnosResponse nonLimitedResults = service.searchDiagnosisByCode(code, ICD_10, Integer.MAX_VALUE - 1);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                is(either(
                        allOf(// Successfully limited amount of results?
                                hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                                hasProperty("diagnoser", hasSize(equalTo(arbitraryChosenUpperBound)))))
                                        .or(allOf(// Or, there were too few results to reach upper limit?
                                                hasProperty("resultat", equalTo(nonLimitedResults.getResultat())),
                                                hasProperty("diagnoser", equalTo(nonLimitedResults.getDiagnoser())),
                                                hasProperty("diagnoser", hasSize(lessThanOrEqualTo(arbitraryChosenUpperBound))))))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsZero() {
        // Given
        String anyString = "";

        // When
        service.searchDiagnosisByCode(anyString, anyString, 0);

        // Then throw IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiagnosServiceWithNbrOfResultsMinusOne() {
        // Given
        String anyString = "";

        // When
        service.searchDiagnosisByCode(anyString, anyString, -1);

        // Then throw IllegalArgumentException
    }

    @Test
    public void testDiagnosCodeValidationCanHandleEmptyArgument() {
        assertFalse("Null should return false", service.validateDiagnosisCode(null, ICD_10));
        assertFalse("Empty string should return false", service.validateDiagnosisCode("", ICD_10));
    }

    @Test
    public void testSearchDiagnosisByDescriptionWithOrdinaryValue() {
        // Given
        String searchTerm = "infekt";
        int greaterThanZero = Integer.MAX_VALUE - 1;

        // When
        DiagnosResponse result = service.searchDiagnosisByDescription(searchTerm, ICD_10, greaterThanZero);

        // Then
        assertThat(result, Matchers.<DiagnosResponse> allOf(
                is(notNullValue()),
                is(either(
                        allOf(
                                hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                                hasProperty("diagnoser", hasSize(greaterThan(0)))))
                                        .or(hasProperty("resultat", equalTo(DiagnosResponseType.NOT_FOUND))))));

        assertEquals("Null should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription(null, ICD_10, 5).getResultat());
        assertEquals("Empty should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription("", ICD_10, 5).getResultat());
        assertEquals("Spaces should return invalid", DiagnosResponseType.INVALID_SEARCH_STRING,
                service.searchDiagnosisByDescription(" ", ICD_10, 5).getResultat());
    }

    @Test
    public void testSearchDiagnosisByDescriptionLimitingAmountOfReturnedResultsIfMoreResultsAreAvailable() {
        // Given
        String searchTerm = "infekt";
        int arbitraryChosenUpperBound = 2;

        // When
        DiagnosResponse result = service.searchDiagnosisByDescription(searchTerm, ICD_10, arbitraryChosenUpperBound);
        DiagnosResponse nonLimitedResults = service.searchDiagnosisByDescription(searchTerm, ICD_10, Integer.MAX_VALUE - 1);

        // Then
        assertThat(result, allOf(
                is(notNullValue()),
                is(either(
                        allOf(// Successfully limited amount of results?
                                hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                                hasProperty("diagnoser", hasSize(equalTo(arbitraryChosenUpperBound)))))
                                        .or(allOf(// Or, there were too few results to reach upper limit?
                                                hasProperty("resultat", equalTo(nonLimitedResults.getResultat())),
                                                hasProperty("diagnoser", equalTo(nonLimitedResults.getDiagnoser())),
                                                hasProperty("diagnoser", hasSize(lessThanOrEqualTo(arbitraryChosenUpperBound))))))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchDiagnosisByDescriptionThrowsExceptionForIntegerMaxValue() {
        service.searchDiagnosisByDescription(null, null, Integer.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchDiagnosisByCodeThrowsExceptionForIntegerMaxValue() {
        service.searchDiagnosisByCode(null, null, Integer.MAX_VALUE);
    }
}
