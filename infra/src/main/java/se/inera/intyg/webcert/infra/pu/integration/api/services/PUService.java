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
package se.inera.intyg.webcert.infra.pu.integration.api.services;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar;

public interface PUService {

  /**
   * Retrieves a person object from the PU-service.
   *
   * <p>If person is not found method will return a PersonSvar object with a null Person object and
   * status NOT_FOUND.
   *
   * <p>If an error occur when calling the PU-service method will return a PersonSvar object a null
   * Person object and with status ERROR.
   *
   * @param personId a Personnummer
   * @return a PersonSvar object with a non-null Person object and status FOUND, otherwise a null
   *     Person object and status NOT_FOUND or ERROR.
   */
  PersonSvar getPerson(Personnummer personId);

  /**
   * Retrieves person objects from the PU-service.
   *
   * <p>For persons found the map will hold a PersonSvar with a non-null Person object and status
   * FOUND. If a person is not found, the map will hold a PersonSvar object with a null Person
   * object and status NOT_FOUND.
   *
   * <p>If an error occur when calling the PU-service method will return an empty map.
   *
   * @param personIds a list of Personnummer
   * @return a map with PersonSvar if call to PU-service was successful, otherwise an empty map.
   */
  Map<Personnummer, PersonSvar> getPersons(List<Personnummer> personIds);

  @VisibleForTesting
  default void clearCache() {}
}
