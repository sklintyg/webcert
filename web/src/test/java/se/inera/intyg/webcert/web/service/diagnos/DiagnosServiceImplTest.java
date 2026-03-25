/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosServiceTest-context.xml")
class DiagnosServiceImplTest {

  @Autowired private DiagnosService service;

  private static final String ICD_10 = Diagnoskodverk.ICD_10_SE.name();

  private static final String KSH97P = Diagnoskodverk.KSH_97_P.name();

  @Test
  void testGetICD10DiagnosisByCode() {
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode(null, ICD_10).getResultat(),
        "Null should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode("", ICD_10).getResultat(),
        "Empty should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode(" ", ICD_10).getResultat(),
        "Spaces should return invalid");

    assertEquals(
        DiagnosResponseType.NOT_FOUND, service.getDiagnosisByCode("A11", ICD_10).getResultat());

    DiagnosResponse res = service.getDiagnosisByCode("A04", ICD_10);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testGetKSH97PDiagnosisByCode() {
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode(null, KSH97P).getResultat(),
        "Null should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode("", KSH97P).getResultat(),
        "Empty should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode(" ", KSH97P).getResultat(),
        "Spaces should return invalid");

    assertEquals(
        DiagnosResponseType.NOT_FOUND, service.getDiagnosisByCode("A11", KSH97P).getResultat());

    DiagnosResponse res = service.getDiagnosisByCode("A00-", KSH97P);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testGetICD10DiagnosisByCodeDiagnoskodverk() {
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode("", Diagnoskodverk.ICD_10_SE).getResultat(),
        "Empty should return invalid");

    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.getDiagnosisByCode("A11", Diagnoskodverk.ICD_10_SE).getResultat());

    DiagnosResponse res = service.getDiagnosisByCode("A04", Diagnoskodverk.ICD_10_SE);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testGetKSH97PDiagnosisByCodeDiagnoskodverk() {
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.getDiagnosisByCode("", Diagnoskodverk.KSH_97_P).getResultat(),
        "Empty should return invalid");

    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.getDiagnosisByCode("A11", Diagnoskodverk.KSH_97_P).getResultat());

    DiagnosResponse res = service.getDiagnosisByCode("A00-", Diagnoskodverk.KSH_97_P);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testGetDiagnosisByCodeOrdinaryValue() {
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
    assertThat(
        result,
        allOf(
            is(notNullValue()),
            is(
                either(
                        allOf(
                            hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                            hasProperty("diagnoser", hasSize(lessThanOrEqualTo(greaterThanZero)))))
                    .or(hasProperty("resultat", equalTo(DiagnosResponseType.NOT_FOUND))))));
  }

  @Test
  void testGetDiagnosisByCodeLimitingAmountOfReturnedResultsIfMoreResultsAreAvailable() {
    // Given
    int arbitraryChosenUpperBound = 2;
    String code = "A04";

    // When
    DiagnosResponse result = service.searchDiagnosisByCode(code, ICD_10, arbitraryChosenUpperBound);
    DiagnosResponse nonLimitedResults =
        service.searchDiagnosisByCode(code, ICD_10, Integer.MAX_VALUE - 1);

    // Then
    assertThat(
        result,
        allOf(
            is(notNullValue()),
            is(
                either(
                        allOf( // Successfully limited amount of results?
                            hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                            hasProperty("diagnoser", hasSize(equalTo(arbitraryChosenUpperBound)))))
                    .or(
                        allOf( // Or, there were too few results to reach upper limit?
                            hasProperty("resultat", equalTo(nonLimitedResults.getResultat())),
                            hasProperty("diagnoser", equalTo(nonLimitedResults.getDiagnoser())),
                            hasProperty(
                                "diagnoser",
                                hasSize(lessThanOrEqualTo(arbitraryChosenUpperBound))))))));
  }

  @Test
  void testDiagnosServiceWithNbrOfResultsZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          // Given
          String anyString = "";

          // When
          service.searchDiagnosisByCode(anyString, anyString, 0);

          // Then throw IllegalArgumentException
        });
  }

  @Test
  void testDiagnosServiceWithNbrOfResultsMinusOne() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          // Given
          String anyString = "";

          // When
          service.searchDiagnosisByCode(anyString, anyString, -1);

          // Then throw IllegalArgumentException
        });
  }

  @Test
  void testSearchDiagnosisByCodeInvalid() {
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.searchDiagnosisByCode("", ICD_10, 1).getResultat());
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.searchDiagnosisByCode("A11", null, 1).getResultat());
    assertEquals(
        DiagnosResponseType.INVALID_CODE,
        service.searchDiagnosisByCode("A11", "unknown-code-system", 1).getResultat());
    assertEquals(
        DiagnosResponseType.INVALID_CODE, service.searchDiagnosisByCode("", null, 1).getResultat());
  }

  @Test
  void testSearchDiagnosisByCodeICD10() {
    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.searchDiagnosisByCode("A11", ICD_10, 1).getResultat());

    DiagnosResponse res = service.searchDiagnosisByCode("A04", ICD_10, 1);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testSearchDiagnosisByCodeKSH97P() {
    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.searchDiagnosisByCode("A11", KSH97P, 1).getResultat());

    DiagnosResponse res = service.searchDiagnosisByCode("A00-", KSH97P, 1);
    assertEquals(DiagnosResponseType.OK, res.getResultat());
    assertFalse(res.getDiagnoser().isEmpty());
  }

  @Test
  void testSearchDiagnosisByDescriptionInvalid() {
    assertEquals(
        DiagnosResponseType.INVALID_SEARCH_STRING,
        service.searchDiagnosisByDescription("", KSH97P, 1).getResultat());
    assertEquals(
        DiagnosResponseType.INVALID_CODE_SYSTEM,
        service.searchDiagnosisByDescription("A11", "unknown-code-system", 1).getResultat());
    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.searchDiagnosisByDescription("345345", ICD_10, 1).getResultat());
    assertEquals(
        DiagnosResponseType.NOT_FOUND,
        service.searchDiagnosisByDescription("345345", KSH97P, 1).getResultat());
    assertEquals(
        DiagnosResponseType.INVALID_CODE_SYSTEM,
        service.searchDiagnosisByDescription("A11", null, 1).getResultat());
  }

  @Test
  void testDiagnosCodeValidationCanHandleEmptyArgument() {
    assertFalse(service.validateDiagnosisCode(null, ICD_10), "Null should return false");
    assertFalse(service.validateDiagnosisCode("", ICD_10), "Empty string should return false");
  }

  @Test
  void testSearchDiagnosisByDescriptionWithOrdinaryValue() {
    // Given
    String searchTerm = "infekt";
    int greaterThanZero = Integer.MAX_VALUE - 1;

    // When
    DiagnosResponse result =
        service.searchDiagnosisByDescription(searchTerm, ICD_10, greaterThanZero);

    // Then
    assertThat(
        result,
        Matchers.<DiagnosResponse>allOf(
            is(notNullValue()),
            is(
                either(
                        allOf(
                            hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                            hasProperty("diagnoser", hasSize(greaterThan(0)))))
                    .or(hasProperty("resultat", equalTo(DiagnosResponseType.NOT_FOUND))))));

    assertEquals(
        DiagnosResponseType.INVALID_SEARCH_STRING,
        service.searchDiagnosisByDescription(null, ICD_10, 5).getResultat(),
        "Null should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_SEARCH_STRING,
        service.searchDiagnosisByDescription("", ICD_10, 5).getResultat(),
        "Empty should return invalid");
    assertEquals(
        DiagnosResponseType.INVALID_SEARCH_STRING,
        service.searchDiagnosisByDescription(" ", ICD_10, 5).getResultat(),
        "Spaces should return invalid");
  }

  @Test
  void testSearchDiagnosisByDescriptionLimitingAmountOfReturnedResultsIfMoreResultsAreAvailable() {
    // Given
    String searchTerm = "infekt";
    int arbitraryChosenUpperBound = 2;

    // When
    DiagnosResponse result =
        service.searchDiagnosisByDescription(searchTerm, ICD_10, arbitraryChosenUpperBound);
    DiagnosResponse nonLimitedResults =
        service.searchDiagnosisByDescription(searchTerm, ICD_10, Integer.MAX_VALUE - 1);

    // Then
    assertThat(
        result,
        allOf(
            is(notNullValue()),
            is(
                either(
                        allOf( // Successfully limited amount of results?
                            hasProperty("resultat", equalTo(DiagnosResponseType.OK)),
                            hasProperty("diagnoser", hasSize(equalTo(arbitraryChosenUpperBound)))))
                    .or(
                        allOf( // Or, there were too few results to reach upper limit?
                            hasProperty("resultat", equalTo(nonLimitedResults.getResultat())),
                            hasProperty("diagnoser", equalTo(nonLimitedResults.getDiagnoser())),
                            hasProperty(
                                "diagnoser",
                                hasSize(lessThanOrEqualTo(arbitraryChosenUpperBound))))))));
  }

  @Test
  void testSearchDiagnosisByDescriptionThrowsExceptionForIntegerMaxValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          service.searchDiagnosisByDescription(null, null, Integer.MAX_VALUE);
        });
  }

  @Test
  void testSearchDiagnosisByCodeThrowsExceptionForIntegerMaxValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          service.searchDiagnosisByCode(null, null, Integer.MAX_VALUE);
        });
  }

  @Test
  void testValidateDiagnosisCodeCodeMissing() {
    boolean res = service.validateDiagnosisCode(null, "ICD_10_SE");

    assertFalse(res);
  }

  @Test
  void testValidateDiagnosisCodeCodeSystemMissing() {
    boolean res = service.validateDiagnosisCode("A11", null);

    assertFalse(res);
  }

  @Test
  void testValidateDiagnosisCodeCodeSystemInvalid() {
    boolean res = service.validateDiagnosisCode("A11", "does-not-exist");

    assertFalse(res);
  }

  @Test
  void testValidateDiagnosisCodeIcd10() throws Exception {
    final List<String> codes = Arrays.asList("A11", "A11.1", "A11.1X", "A111", "A111X", "A1111");

    for (String code : codes) {
      final boolean result = service.validateDiagnosisCode(code, "ICD_10_SE");

      assertTrue(result);
    }
  }

  @Test
  void testValidateDiagnosisCodeKsh97p() throws Exception {
    final List<String> codes = Arrays.asList("A11", "A11-P", "A11-", "A111", "A111P", "F438A");

    for (String code : codes) {
      final boolean result = service.validateDiagnosisCode(code, "KSH_97_P");

      assertTrue(result);
    }
  }

  @Test
  void testValidateDiagnosisCodeFormat() {
    assertTrue(service.validateDiagnosisCodeFormat("U071"));
    assertFalse(service.validateDiagnosisCodeFormat("U07.1"));
  }
}
