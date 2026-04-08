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
package se.inera.intyg.webcert.infra.integration.hsatk.services.legacy;

import java.util.List;
import se.inera.intyg.webcert.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserAuthorizationInfo;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;

public interface HsaOrganizationsService {

  /**
   * Returns a list of Vardgivare and authorized enheter where the HoS person is authorized to work
   * at.
   *
   * @return list of vårdgivare containing authorized enheter and mottagningar. If user is not
   *     authorized at all, an empty list will be returned
   */
  UserAuthorizationInfo getAuthorizedEnheterForHosPerson(String hosPersonHsaId);

  /**
   * Returns the hsaId of the parent care giver of the specified care unit.
   *
   * @param vardenhetHsaId HsaId of the vårdenhet.
   * @return HsaId of the parent vårdgivare. If no vårdgivare could be found, null is returned.
   */
  String getVardgivareOfVardenhet(String vardenhetHsaId);

  /**
   * Returns a fully recursively populated Vardenhet for the specified hsaId.
   *
   * @param vardenhetHsaId HsaId of the vårdenhet.
   * @return The Vardenhet.
   */
  Vardenhet getVardenhet(String vardenhetHsaId);

  /** Returns a shallow representation of a Vardgivare, which does NOT contain any Vardenheter. */
  Vardgivare getVardgivareInfo(String vardgivareHsaId);

  /**
   * Returns a list of hsaId's for all (any) sub units (mottagningar) on the specified care unit.
   *
   * @param vardEnhetHsaId HsaId of the vårdenhet.
   * @return A list of hsaId's for mottagningar.
   */
  List<String> getHsaIdForAktivaUnderenheter(String vardEnhetHsaId);

  String getParentUnit(String hsaId) throws HsaServiceCallException;
}
