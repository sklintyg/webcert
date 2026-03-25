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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation.PaTitle;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;

public class IntygUser implements UserDetails {

  public static final int THIRTYONE = 31;
  private static final long serialVersionUID = -2624303818412468774L;
  protected boolean userTermsApprovedOrSubscriptionInUse;

  protected String personId;
  protected String hsaId;
  protected String fornamn;
  protected String efternamn;
  protected String namn;
  protected String titel;
  protected String forskrivarkod;
  protected String authenticationScheme;
  protected boolean isSekretessMarkerad;

  protected List<Vardgivare> vardgivare;
  protected Map<String, String> miuNamnPerEnhetsId = new HashMap<>();
  protected List<String> befattningar;
  protected List<PaTitle> befattningsKoder;
  protected List<String> specialiseringar;
  protected List<String> legitimeradeYrkesgrupper;
  protected List<String> systemRoles;

  protected SelectableVardenhet valdVardenhet;
  protected SelectableVardenhet valdVardgivare;

  protected AuthenticationMethod authenticationMethod;

  // Fields related to the authority context
  protected Map<String, Feature> features = new HashMap<>();
  protected Map<String, Role> roles = new HashMap<>();
  protected Map<String, Privilege> authorities = new HashMap<>();
  protected String origin;
  protected String roleTypeName;

  /** The sole constructor. */
  public IntygUser(String employeeHsaId) {
    this.hsaId = employeeHsaId;
  }

  public boolean changeValdVardenhet(String vardenhetId) {
    if (vardenhetId == null) {
      return false;
    }

    for (Vardgivare vg : getVardgivare()) {
      SelectableVardenhet ve = vg.findVardenhet(vardenhetId);
      if (ve != null) {
        setValdVardenhet(ve);
        setValdVardgivare(vg);
        return true;
      }
    }

    return false;
  }

  @Override
  public Map<String, Feature> getFeatures() {
    if (features == null) {
      features = new HashMap<>();
    }
    return features;
  }

  @Override
  public void setFeatures(Map<String, Feature> features) {
    this.features = features;
  }

  public boolean isFeatureActive(String featureName) {
    return features != null
        && features.containsKey(featureName)
        && features.get(featureName).getGlobal();
  }

  @JsonIgnore
  public String getAsJson() {
    try {
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());
      om.setSerializationInclusion(Include.NON_NULL);
      return om.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public AuthenticationMethod getAuthenticationMethod() {
    return authenticationMethod;
  }

  public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
    this.authenticationMethod = authenticationMethod;
  }

  public String getAuthenticationScheme() {
    return authenticationScheme;
  }

  public void setAuthenticationScheme(String authenticationScheme) {
    this.authenticationScheme = authenticationScheme;
  }

  public boolean isSekretessMarkerad() {
    return isSekretessMarkerad;
  }

  public void setSekretessMarkerad(boolean sekretessMarkerad) {
    isSekretessMarkerad = sekretessMarkerad;
  }

  @Override
  public String getOrigin() {
    return origin;
  }

  @Override
  public void setOrigin(String origin) {
    this.origin = origin;
  }

  @JsonIgnore
  public String getRoleTypeName() {
    return roleTypeName;
  }

  public void setRoleTypeName(String roleTypeName) {
    this.roleTypeName = roleTypeName;
  }

  /**
   * Returns the privileges granted to the user. Cannot return <code>null</code>.
   *
   * @return the privileges, sorted by natural key (never <code>null</code>)
   */
  @Override
  public Map<String, Privilege> getAuthorities() {
    return this.authorities;
  }

  /** Set the authorities/privileges granted to a user. */
  @Override
  public void setAuthorities(Map<String, Privilege> authorities) {
    this.authorities = authorities;
  }

  public String getForskrivarkod() {
    return forskrivarkod;
  }

  public void setForskrivarkod(String forskrivarkod) {
    this.forskrivarkod = forskrivarkod;
  }

  public String getHsaId() {
    return hsaId;
  }

  public void setHsaId(String hsaId) {
    this.hsaId = hsaId;
  }

  @JsonIgnore
  public List<String> getIdsOfAllVardenheter() {
    List<String> allIds = new ArrayList<>();
    for (Vardgivare v : getVardgivare()) {
      allIds.addAll(v.getHsaIds());
    }
    return allIds;
  }

  @JsonIgnore
  public List<String> getIdsOfSelectedVardenhet() {

    SelectableVardenhet selected = getValdVardenhet();

    if (selected == null) {
      return Collections.emptyList();
    }

    return selected.getHsaIds();
  }

  @JsonIgnore
  public List<String> getIdsOfSelectedVardgivare() {

    SelectableVardenhet selected = getValdVardgivare();

    if (selected == null) {
      return Collections.emptyList();
    }

    return selected.getHsaIds();
  }

  public List<String> getLegitimeradeYrkesgrupper() {
    if (legitimeradeYrkesgrupper == null) {
      legitimeradeYrkesgrupper = Collections.emptyList();
    }

    return legitimeradeYrkesgrupper;
  }

  public void setLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
    this.legitimeradeYrkesgrupper = legitimeradeYrkesgrupper;
  }

  public List<String> getSystemRoles() {
    if (systemRoles == null) {
      systemRoles = Collections.emptyList();
    }
    return systemRoles;
  }

  public void setSystemRoles(List<String> systemRoles) {
    this.systemRoles = systemRoles;
  }

  /**
   * Returns the name to authenticate the user. Cannot return <code>null</code>.
   *
   * @return the name (never <code>null</code>)
   */
  @Override
  public String getNamn() {
    return this.namn;
  }

  public void setNamn(String namn) {
    this.namn = namn;
  }

  @Override
  public String getFornamn() {
    return this.fornamn;
  }

  public void setFornamn(String fornamn) {
    this.fornamn = fornamn;
  }

  @Override
  public String getEfternamn() {
    return efternamn;
  }

  public void setEfternamn(String efternamn) {
    this.efternamn = efternamn;
  }

  /**
   * Returns the personal identifier used to authenticate the user.
   *
   * @return the personal identifier
   */
  @Override
  public String getPersonId() {
    return this.personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  /**
   * Returns the role granted to the user. Cannot return <code>null</code>.
   *
   * @return the role, sorted by natural key (never <code>null</code>)
   */
  @Override
  public Map<String, Role> getRoles() {
    return this.roles;
  }

  /** Set the roles granted to a user. */
  @Override
  public void setRoles(Map<String, Role> roles) {
    this.roles = roles;
  }

  public List<String> getSpecialiseringar() {
    if (specialiseringar == null) {
      specialiseringar = Collections.emptyList();
    }

    return specialiseringar;
  }

  public void setSpecialiseringar(List<String> specialiseringar) {
    this.specialiseringar = specialiseringar;
  }

  public List<String> getBefattningar() {
    if (befattningar == null) {
      befattningar = Collections.emptyList();
    }
    return befattningar;
  }

  public List<PaTitle> getBefattningsKoder() {
    if (befattningsKoder == null) {
      befattningsKoder = Collections.emptyList();
    }
    return befattningsKoder;
  }

  public void setBefattningar(List<String> befattningar) {
    this.befattningar = befattningar;
  }

  public void setBefattningsKoder(List<PaTitle> befattningsKoder) {
    this.befattningsKoder = befattningsKoder;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(String titel) {
    this.titel = titel;
  }

  public int getTotaltAntalVardenheter() {
    return getIdsOfAllVardenheter().size();
  }

  public SelectableVardenhet getValdVardenhet() {
    return valdVardenhet;
  }

  public void setValdVardenhet(SelectableVardenhet valdVardenhet) {
    this.valdVardenhet = valdVardenhet;
  }

  public SelectableVardenhet getValdVardgivare() {
    return valdVardgivare;
  }

  public void setValdVardgivare(SelectableVardenhet valdVardgivare) {
    this.valdVardgivare = valdVardgivare;
  }

  public List<Vardgivare> getVardgivare() {
    if (vardgivare == null) {
      vardgivare = Collections.emptyList();
    }

    return vardgivare;
  }

  public void setVardgivare(List<Vardgivare> vardgivare) {
    this.vardgivare = vardgivare;
  }

  @JsonIgnore
  public Map<String, String> getMiuNamnPerEnhetsId() {
    return miuNamnPerEnhetsId;
  }

  public void setMiuNamnPerEnhetsId(Map<String, String> miuNamnPerEnhetsId) {
    this.miuNamnPerEnhetsId = miuNamnPerEnhetsId;
  }

  /**
   * Utility method to get the name "medarbetaruppdrag" that the user has on the currently selected
   * vårdenhet.
   *
   * @return The name of the medarbetaruppdrag. (Derived from
   *     infrastructure:directory:authorizationmanagement CommissionType#commissionName)
   * @throws IllegalStateException if no vardenhet is selected or if the map that maps enhetsId to
   *     commissionName hasn't been initialized.
   */
  @JsonIgnore
  public String getSelectedMedarbetarUppdragNamn() {
    if (valdVardenhet == null) {
      throw new IllegalStateException(
          "Cannot resolve current medarbetaruppdrag name, no vardenhet selected.");
    }
    if (miuNamnPerEnhetsId == null) {
      throw new IllegalStateException(
          "Cannot resolve current medarbetaruppdrag name, map of MiU's not initialized.");
    }

    // Try to get commissionName directly from the selected care unit.
    // If not possible, check if we're a Mottagning and use the parentHsaId.
    if (miuNamnPerEnhetsId.containsKey(valdVardenhet.getId())) {
      return miuNamnPerEnhetsId.get(valdVardenhet.getId());
    } else {
      if (valdVardenhet instanceof Mottagning) {
        String parentHsaId = ((Mottagning) valdVardenhet).getParentHsaId();
        return miuNamnPerEnhetsId.get(parentHsaId);
      } else {
        return null;
      }
    }
  }

  /**
   * Determines if the user's roles contains a lakare role or not.
   *
   * <ul>
   *   The following roles are considered lakare:
   *   <li>ROLE_LAKARE
   *   <li>ROLE_PRIVATLAKARE
   *   <li>ROLE_TANDLAKARE
   * </ul>
   *
   * Note: This construct smells a bit, as it's somewhat ambigous what isLakare could be interpreted
   * as?
   *
   * @return true if role is one the above, otherwise false
   */
  public boolean isLakare() {
    return roles.containsKey(AuthoritiesConstants.ROLE_LAKARE)
        || roles.containsKey(AuthoritiesConstants.ROLE_PRIVATLAKARE)
        || roles.containsKey(AuthoritiesConstants.ROLE_TANDLAKARE);
  }

  public boolean isPrivatLakare() {
    return roles.containsKey(AuthoritiesConstants.ROLE_PRIVATLAKARE);
  }

  public boolean isUserTermsApprovedOrSubscriptionInUse() {
    return userTermsApprovedOrSubscriptionInUse;
  }

  public void setUserTermsApprovedOrSubscriptionInUse(
      boolean userTermsApprovedOrSubscriptionInUse) {
    this.userTermsApprovedOrSubscriptionInUse = userTermsApprovedOrSubscriptionInUse;
  }

  // CHECKSTYLE:OFF NeedBraces

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntygUser)) {
      return false;
    }

    IntygUser intygUser = (IntygUser) o;

    if (userTermsApprovedOrSubscriptionInUse != intygUser.userTermsApprovedOrSubscriptionInUse) {
      return false;
    }
    if (isSekretessMarkerad != intygUser.isSekretessMarkerad) {
      return false;
    }
    if (personId != null ? !personId.equals(intygUser.personId) : intygUser.personId != null) {
      return false;
    }
    if (!hsaId.equals(intygUser.hsaId)) {
      return false;
    }
    if (namn != null ? !namn.equals(intygUser.namn) : intygUser.namn != null) {
      return false;
    }
    if (titel != null ? !titel.equals(intygUser.titel) : intygUser.titel != null) {
      return false;
    }
    if (forskrivarkod != null
        ? !forskrivarkod.equals(intygUser.forskrivarkod)
        : intygUser.forskrivarkod != null) {
      return false;
    }
    if (!authenticationScheme.equals(intygUser.authenticationScheme)) {
      return false;
    }
    if (vardgivare != null
        ? !vardgivare.equals(intygUser.vardgivare)
        : intygUser.vardgivare != null) {
      return false;
    }
    if (miuNamnPerEnhetsId != null
        ? !miuNamnPerEnhetsId.equals(intygUser.miuNamnPerEnhetsId)
        : intygUser.miuNamnPerEnhetsId != null) {
      return false;
    }
    if (befattningar != null
        ? !befattningar.equals(intygUser.befattningar)
        : intygUser.befattningar != null) {
      return false;
    }
    if (specialiseringar != null
        ? !specialiseringar.equals(intygUser.specialiseringar)
        : intygUser.specialiseringar != null) {
      return false;
    }
    if (legitimeradeYrkesgrupper != null
        ? !legitimeradeYrkesgrupper.equals(intygUser.legitimeradeYrkesgrupper)
        : intygUser.legitimeradeYrkesgrupper != null) {
      return false;
    }
    if (systemRoles != null
        ? !systemRoles.equals(intygUser.systemRoles)
        : intygUser.systemRoles != null) {
      return false;
    }
    if (valdVardenhet != null
        ? !valdVardenhet.equals(intygUser.valdVardenhet)
        : intygUser.valdVardenhet != null) {
      return false;
    }
    if (valdVardgivare != null
        ? !valdVardgivare.equals(intygUser.valdVardgivare)
        : intygUser.valdVardgivare != null) {
      return false;
    }
    if (authenticationMethod != intygUser.authenticationMethod) {
      return false;
    }
    if (features != null ? !features.equals(intygUser.features) : intygUser.features != null) {
      return false;
    }
    if (roles != null ? !roles.equals(intygUser.roles) : intygUser.roles != null) {
      return false;
    }
    if (authorities != null
        ? !authorities.equals(intygUser.authorities)
        : intygUser.authorities != null) {
      return false;
    }
    if (roleTypeName != null
        ? !roleTypeName.equals(intygUser.roleTypeName)
        : intygUser.roleTypeName != null) {
      return false;
    }
    return (origin != null ? !origin.equals(intygUser.origin) : intygUser.origin != null);
  }

  @Override
  public int hashCode() {
    int result = (userTermsApprovedOrSubscriptionInUse ? 1 : 0);
    result = THIRTYONE * result + (personId != null ? personId.hashCode() : 0);
    result = THIRTYONE * result + hsaId.hashCode();
    result = THIRTYONE * result + (namn != null ? namn.hashCode() : 0);
    result = THIRTYONE * result + (titel != null ? titel.hashCode() : 0);
    result = THIRTYONE * result + (forskrivarkod != null ? forskrivarkod.hashCode() : 0);
    result = THIRTYONE * result + authenticationScheme.hashCode();
    result = THIRTYONE * result + (isSekretessMarkerad ? 1 : 0);
    result = THIRTYONE * result + (vardgivare != null ? vardgivare.hashCode() : 0);
    result = THIRTYONE * result + (miuNamnPerEnhetsId != null ? miuNamnPerEnhetsId.hashCode() : 0);
    result = THIRTYONE * result + (befattningar != null ? befattningar.hashCode() : 0);
    result = THIRTYONE * result + (specialiseringar != null ? specialiseringar.hashCode() : 0);
    result =
        THIRTYONE * result
            + (legitimeradeYrkesgrupper != null ? legitimeradeYrkesgrupper.hashCode() : 0);
    result = THIRTYONE * result + (systemRoles != null ? systemRoles.hashCode() : 0);
    result = THIRTYONE * result + (valdVardenhet != null ? valdVardenhet.hashCode() : 0);
    result = THIRTYONE * result + (valdVardgivare != null ? valdVardgivare.hashCode() : 0);
    result =
        THIRTYONE * result + (authenticationMethod != null ? authenticationMethod.hashCode() : 0);
    result = THIRTYONE * result + (features != null ? features.hashCode() : 0);
    result = THIRTYONE * result + (roles != null ? roles.hashCode() : 0);
    result = THIRTYONE * result + (authorities != null ? authorities.hashCode() : 0);
    result = THIRTYONE * result + (origin != null ? origin.hashCode() : 0);
    result = THIRTYONE * result + (roleTypeName != null ? roleTypeName.hashCode() : 0);
    return result;
  }

  // CHECKSTYLE:ON NeedBraces

  @JsonIgnore
  @Override
  public String toString() {
    return hsaId + " [authScheme=" + authenticationScheme + ", lakare=" + isLakare() + "]";
  }
}
