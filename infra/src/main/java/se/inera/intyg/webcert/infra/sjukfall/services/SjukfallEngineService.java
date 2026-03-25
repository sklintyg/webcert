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
package se.inera.intyg.webcert.infra.sjukfall.services;

import java.util.List;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.SjukfallPatient;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public interface SjukfallEngineService {

  /**
   * This method is the entry point when calculating 'sjukfall' for a health care untit. Provided
   * data are certificate information and request parameters set by the client. Each compiled
   * 'sjukfall' corresponds to one patient.
   *
   * @param intygData the certificate information (base data) for the health care unit
   * @param parameters client request parameters
   * @return a list of compiled 'sjukfall'.
   */
  List<SjukfallEnhet> beraknaSjukfallForEnhet(
      List<IntygData> intygData, IntygParametrar parameters);

  /**
   * This method is the entry point when calculating 'sjukfall' for one patient. Provied data are
   * certificate information and request parameters set by the client.
   *
   * @param intygData the certificate information (base data) for one patient
   * @param parameters client request parameters
   * @return a list of compiled 'sjukfall'.
   */
  List<SjukfallPatient> beraknaSjukfallForPatient(
      List<IntygData> intygData, IntygParametrar parameters);
}
