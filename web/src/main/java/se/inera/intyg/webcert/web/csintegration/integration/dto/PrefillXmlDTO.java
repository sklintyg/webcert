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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;
import se.riv.clinicalprocess.healthcond.certificate.v33.ObjectFactory;

@Value
@Slf4j
@AllArgsConstructor
public class PrefillXmlDTO {

  String value;

  public static PrefillXmlDTO marshall(Forifyllnad forifyllnad) {
    try {
      final var element = new ObjectFactory().createForifyllnad(forifyllnad);

      final var stringWriter = new StringWriter();
      final var jaxbContext = JAXBContext.newInstance(Forifyllnad.class);
      final var marshaller = jaxbContext.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
      marshaller.marshal(element, stringWriter);

      final var xml = Base64.getEncoder()
          .encodeToString(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
      return new PrefillXmlDTO(xml);
    } catch (Exception e) {
      log.error("Failed to marshal Forifyllnad to XML", e);
      return null;
    }
  }
}
