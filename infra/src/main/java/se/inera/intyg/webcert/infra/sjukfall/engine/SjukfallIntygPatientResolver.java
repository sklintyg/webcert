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
package se.inera.intyg.webcert.infra.sjukfall.engine;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;
import se.inera.intyg.webcert.infra.sjukfall.dto.SjukfallIntyg;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
public class SjukfallIntygPatientResolver {

  private static final Logger LOG = LoggerFactory.getLogger(SjukfallIntygPatientResolver.class);

  private SjukfallIntygPatientCreator creator;

  // constructor

  public SjukfallIntygPatientResolver(SjukfallIntygPatientCreator creator) {
    this.creator = creator;
  }

  // api

  /**
   * Method is resolving sjukfall for a health care unit based on the unit's certificate
   * information. A map with patient id as key and a list of certificates associated with a sjukfall
   * as value, will be returned.
   */
  public Map<Integer, List<SjukfallIntyg>> resolve(
      final List<IntygData> intygsData, final int maxIntygsGlapp, final LocalDate aktivtDatum) {

    LOG.debug("Start resolving certificate information...");
    LOG.debug(
        "  - max days between certificates: {}, active date: {}", maxIntygsGlapp, aktivtDatum);

    if (intygsData == null || intygsData.isEmpty()) {
      LOG.info("There was no in-data! Returning empty list");
      return new HashMap<>();
    }

    if (maxIntygsGlapp < 0) {
      LOG.info(
          "Maximal days between certificates was {}. Value must be equal or greater than zero",
          maxIntygsGlapp);
      return new HashMap<>();
    }

    // Create a map of sjukfall
    Map<Integer, List<SjukfallIntyg>> sjukfall =
        creator.create(intygsData, maxIntygsGlapp, aktivtDatum);

    LOG.debug("...stop resolving certificate information.");
    return sjukfall;
  }
}
