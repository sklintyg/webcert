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
package se.inera.intyg.webcert.infra.integration.hsatk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation.PaTitle;

class HsaAttributeExtractorTest {

  private HsaAttributeExtractor extractor;

  @BeforeEach
  void setUp() {
    extractor = new HsaAttributeExtractor();
  }

  @Nested
  class ExtractBefattningsKoder {

    @Test
    void shouldReturnEmptyListWhenInputIsEmpty() {
      final var result = extractor.extractBefattningsKoder(Collections.emptyList());

      assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterOutPersonInformationWithNullPaTitle() {
      final var personWithNullPaTitle = new PersonInformation();
      personWithNullPaTitle.setPaTitle(null);

      final var personWithValidPaTitle =
          createPersonInformationWithPaTitles(createPaTitle("Läkare - AT", "402010"));

      final var input = Arrays.asList(personWithNullPaTitle, personWithValidPaTitle);
      final var result = extractor.extractBefattningsKoder(input);

      assertEquals(1, result.size());
    }

    @Test
    void shouldFilterOutPaTitlesWithNullPaTitleCode() {
      final var validPaTitle = createPaTitle("Läkare - AT", "402010");
      final var paTitleWithNullCode = createPaTitle("Sjuksköterska", null);

      final var personInfo = createPersonInformationWithPaTitles(validPaTitle, paTitleWithNullCode);
      final var input = Collections.singletonList(personInfo);

      final var result = extractor.extractBefattningsKoder(input);

      assertEquals("402010", result.getFirst().getPaTitleCode());
    }

    @Test
    void shouldRemoveDuplicatePaTitles() {
      final var paTitleL = createPaTitle("Läkare - AT", "402010");
      final var paTitleL2 = createPaTitle("Läkare - AT", "402010");

      final var person1 = createPersonInformationWithPaTitles(paTitleL, paTitleL2);

      final var input = List.of(person1);
      final var result = extractor.extractBefattningsKoder(input);

      assertEquals(1, result.size(), "Expected only one unique PaTitle");
    }

    @Test
    void shouldSortResultsByPaTitleCode() {
      final var paTitleB = createPaTitle("Barnmorska", "609050");
      final var paTitleL = createPaTitle("Läkare - AT", "402010");
      final var paTitleS = createPaTitle("Sjuksköterska", "203040");

      final var personInfo = createPersonInformationWithPaTitles(paTitleB, paTitleL, paTitleS);
      final var input = Collections.singletonList(personInfo);

      final var result = extractor.extractBefattningsKoder(input);

      assertEquals("203040", result.getFirst().getPaTitleCode());
    }

    @Test
    void shouldReturnCorrectPaTitleObjects() {
      final var expectedPaTitle = createPaTitle("Läkare - ST", "502010");
      final var personInfo = createPersonInformationWithPaTitles(expectedPaTitle);
      final var input = Collections.singletonList(personInfo);

      final var result = extractor.extractBefattningsKoder(input);

      final var actualPaTitle = result.getFirst();

      assertEquals(expectedPaTitle, actualPaTitle);
    }

    @Test
    void shouldHandleMixedValidAndInvalidData() {
      final var validPaTitleL = createPaTitle("Läkare - AT", "402010");
      final var validPaTitleS = createPaTitle("Sjuksköterska", "203040");
      final var paTitleWithNullCode = createPaTitle("Okänd", null);

      final var personWithValidTitles =
          createPersonInformationWithPaTitles(validPaTitleL, validPaTitleS);
      final var personWithInvalidTitleCode =
          createPersonInformationWithPaTitles(paTitleWithNullCode);
      final var personWithNullPaTitle = new PersonInformation();
      personWithNullPaTitle.setPaTitle(null);

      final var input =
          Arrays.asList(personWithValidTitles, personWithInvalidTitleCode, personWithNullPaTitle);
      final var result = extractor.extractBefattningsKoder(input);

      assertEquals(2, result.size());
    }

    private PersonInformation createPersonInformationWithPaTitles(PaTitle... paTitles) {
      final var personInfo = new PersonInformation();
      final var paTitleList = new ArrayList<>(Arrays.asList(paTitles));
      personInfo.setPaTitle(paTitleList);
      return personInfo;
    }

    private PaTitle createPaTitle(String name, String code) {
      final var paTitle = new PaTitle();
      paTitle.setPaTitleName(name);
      paTitle.setPaTitleCode(code);
      return paTitle;
    }
  }
}
