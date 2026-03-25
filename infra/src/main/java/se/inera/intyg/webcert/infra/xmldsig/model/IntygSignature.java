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
package se.inera.intyg.webcert.infra.xmldsig.model;

import java.io.Serializable;

public interface IntygSignature extends Serializable {

  /** Typically the canonicalized intyg XML element or plain JSON. */
  String getCanonicalizedIntyg();

  /**
   * This should be what goes into any signing function (in NetiD plugin, NetiD Access or GRP).
   *
   * <p>Note that the SignedInfo XML goes undigested into NetiD plugin sign(..), just
   * Base64-encoded.
   */
  String getSigningData();

  /** The JSON representation before adding signature. Used for digests etc. */
  String getIntygJson();
}
