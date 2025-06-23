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

package se.inera.intyg.webcert.web.csintegration.integration.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import se.riv.clinicalprocess.healthcond.certificate.v3.Svar;
import se.riv.clinicalprocess.healthcond.certificate.v3.Svar.Delsvar;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;

class PrefillXmlDTOTest {

  @Test
  void shouldMarshallPrefillToXml() {
    Forifyllnad forifyllnad = new Forifyllnad();
    final var svar = new Svar();
    svar.setId("testSvarId");
    final var delsvar = new Delsvar();
    svar.getDelsvar().add(delsvar);

    forifyllnad.getSvar().add(svar);
    PrefillXmlDTO prefillXmlDTO = PrefillXmlDTO.marshall(forifyllnad);
    String xml = new String(Base64.getDecoder().decode(prefillXmlDTO.getValue()),
        StandardCharsets.UTF_8);

    assertTrue(xml.contains("testSvarId"));
  }

}
