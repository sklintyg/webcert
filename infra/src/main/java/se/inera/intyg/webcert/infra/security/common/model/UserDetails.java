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
package se.inera.intyg.webcert.infra.security.common.model;

import java.io.Serializable;
import java.util.Map;
import se.inera.intyg.infra.security.common.model.Role;

/** Created by Magnus Ekstrand on 2015-08-27. */
public interface UserDetails extends Serializable {

  String getFornamn();

  String getEfternamn();

  /**
   * Returns the name to authenticate the user. Cannot return <code>null</code>.
   *
   * @return the name (never <code>null</code>)
   */
  String getNamn();

  /**
   * Returns the personal identifier used to authenticate the user.
   *
   * @return the personal identifier
   */
  String getPersonId();

  /**
   * Gets the features the system has put on the user.
   *
   * @return the features
   */
  Map<String, Feature> getFeatures();

  /** Sets the system wide features on user. */
  void setFeatures(Map<String, Feature> features);

  /**
   * Returns the role granted to the user. Cannot return <code>null</code>.
   *
   * @return the role
   */
  Map<String, Role> getRoles();

  /** Set the roles granted to a user. */
  void setRoles(Map<String, Role> roles);

  /**
   * Returns the authorities (privileges) granted to the user. Cannot return <code>null</code>.
   *
   * @return the authorities, sorted by natural key (never <code>null</code>)
   */
  Map<String, Privilege> getAuthorities();

  /** Set the authorities (privileges) granted to a user. */
  void setAuthorities(Map<String, Privilege> authorities);

  /**
   * Get user's origin, i.e his/hers entrance to the system.
   *
   * @return the user's origin.
   */
  String getOrigin();

  /** Set user's origin. */
  void setOrigin(String origin);
}
