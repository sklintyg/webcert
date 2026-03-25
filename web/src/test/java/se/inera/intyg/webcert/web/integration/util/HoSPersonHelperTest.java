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
package se.inera.intyg.webcert.web.integration.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation.PaTitle;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class HoSPersonHelperTest {

  @Nested
  class EnrichHoSPerson {

    @Test
    void enrichHoSPersonShouldUseBefattningsKoderWhenPresent() {
      final var intygUser = buildIntygUser();
      final var befattning = buildPaTitle();

      intygUser.setBefattningsKoder(List.of(befattning));

      final var hoSPersonal = new HoSPersonal();

      HoSPersonHelper.enrichHoSPerson(hoSPersonal, intygUser);

      assertAll(
          () -> assertEquals(1, hoSPersonal.getBefattningsKoder().size()),
          () -> assertTrue(hoSPersonal.getBefattningar().isEmpty()));
    }

    @Test
    void enrichHoSPersonShouldFallbackToBefattningarWhenNoBefattningsKoder() {
      final var intygUser = buildIntygUser();
      intygUser.setBefattningar(List.of("402010", "402020"));

      final var hoSPersonal = new HoSPersonal();

      HoSPersonHelper.enrichHoSPerson(hoSPersonal, intygUser);

      assertAll(
          () -> assertEquals(2, hoSPersonal.getBefattningar().size()),
          () -> assertTrue(hoSPersonal.getBefattningsKoder().isEmpty()));
    }

    @Test
    void enrichHoSPersonShouldFilterOutDuplicateBefattningsKoder() {
      final var intygUser = buildIntygUser();
      final var befattning = buildPaTitle("402010", "Läkare - AT");

      intygUser.setBefattningsKoder(List.of(befattning, befattning));

      final var hoSPersonal = new HoSPersonal();

      HoSPersonHelper.enrichHoSPerson(hoSPersonal, intygUser);

      assertEquals(1, hoSPersonal.getBefattningsKoder().size());
    }
  }

  private IntygUser buildIntygUser() {
    final var user = new WebCertUser();
    user.setTitel("Läkare");
    user.setSpecialiseringar(List.of("Kardiologi", "Internmedicin"));
    user.setBefattningar(List.of("Överläkare", "Specialist"));
    user.setValdVardenhet(buildVardenhet());
    return user;
  }

  private PaTitle buildPaTitle() {
    return buildPaTitle("402010", "Läkare - AT");
  }

  private PaTitle buildPaTitle(String code, String name) {
    final var paTitle = new PersonInformation.PaTitle();
    paTitle.setPaTitleCode(code);
    paTitle.setPaTitleName(name);
    return paTitle;
  }

  public static SelectableVardenhet buildVardenhet() {
    final var enhet = new Vardenhet();
    enhet.setId("123");
    enhet.setNamn("Enhetsnamn");
    enhet.setEpost("test@test.com");
    enhet.setTelefonnummer("12345");
    enhet.setPostadress("Enhetsadress");
    enhet.setPostnummer("12345");
    enhet.setPostort("Enhetsort");
    enhet.setArbetsplatskod("000000");
    return enhet;
  }
}
