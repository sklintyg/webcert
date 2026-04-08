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
package se.inera.intyg.webcert.infra.security.common.service;

import java.util.HashSet;
import java.util.Set;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;

/**
 * Helper class for checking whether a {@link IntygUser} has access to a given vardenhet/mottagning.
 *
 * <p>Remember that having medarbetaruppdrag on a Vardenhet implicitly gives full access to all its
 * mottagninar.
 *
 * <p>Created by eriklupander on 2017-09-18.
 */
public final class CareUnitAccessHelper {

  private CareUnitAccessHelper() {}

  /**
   * Since the WebCertUser#getValdVardenhet may either return a {@link
   * se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet} or a {@link
   * se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Mottagning}, this method can be
   * used to determine if:
   *
   * <ul>
   *   <li>If the selectedVardenhet is a Vardenhet: The supplied enhetsId is for the Vardenhet or
   *       one of its Mottagningar.
   *   <li>If the selcetedVardenhet is a Mottagning: The supplied enhetsId is the Mottagning, its
   *       parent Vardenhet or one of the sibling Mottagningar.
   * </ul>
   *
   * @param enhetsId HSA-id of a vardenhet or mottagning.
   * @return true if match is found.
   */
  public static boolean userIsLoggedInOnEnhetOrUnderenhet(IntygUser user, String enhetsId) {

    SelectableVardenhet valdVardenhet = user.getValdVardenhet();
    Set<String> allowedEnhetsId = new HashSet<>();
    if (valdVardenhet instanceof Vardenhet) {
      Vardenhet vardenhet = (Vardenhet) valdVardenhet;
      allowedEnhetsId.add(vardenhet.getId());
      vardenhet.getMottagningar().stream().forEach(m -> allowedEnhetsId.add(m.getId()));
    } else if (valdVardenhet instanceof Mottagning) {
      Mottagning mottagning = (Mottagning) valdVardenhet;

      for (Vardgivare vg : user.getVardgivare()) {
        for (Vardenhet ve : vg.getVardenheter()) {
          if (ve.getId().equals(mottagning.getParentHsaId())) {
            allowedEnhetsId.add(ve.getId());
            ve.getMottagningar().stream().forEach(m -> allowedEnhetsId.add(m.getId()));
          }
        }
      }
    }
    return allowedEnhetsId.contains(enhetsId);
  }
}
