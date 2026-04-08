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
package se.inera.intyg.webcert.infra.security.authorities;

import static se.inera.intyg.webcert.infra.security.authorities.AuthoritiesResolverUtil.toMap;
import static se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants.TITLECODE_AT_LAKARE;
import static se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants.TITLECODE_BT_LAKARE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.webcert.infra.integration.hsatk.model.PersonInformation.PaTitle;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.UserCredentials;
import se.inera.intyg.webcert.infra.integration.hsatk.util.HsaAttributeExtractor;
import se.inera.intyg.webcert.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.security.common.model.Pilot;
import se.inera.intyg.webcert.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.infra.security.common.model.Role;
import se.inera.intyg.webcert.infra.security.common.model.RoleResolveResult;
import se.inera.intyg.webcert.infra.security.common.model.Title;
import se.inera.intyg.webcert.infra.security.common.model.TitleCode;

/** Created by Magnus Ekstrand on 20/11/15. */
@Service
public class CommonAuthoritiesResolver {

  private static final Logger LOG = LoggerFactory.getLogger(CommonAuthoritiesResolver.class);

  @Autowired private SecurityConfigurationLoader configurationLoader;
  private Function<String, RequestOrigin> fnRequestOrigin =
      (name) -> getRequestOrigins().stream().filter(isRequestOrigin(name)).findFirst().orElse(null);
  private Function<String, Role> fnRole =
      (name) -> getRoles().stream().filter(isRole(name)).findFirst().orElse(null);
  private BiFunction<String, String, TitleCode> fnTitleCode =
      (titleCode, groupPrescriptionCode) ->
          getTitleCodes().stream()
              .filter(isTitleCode(titleCode).and(isGroupPrescriptionCode(groupPrescriptionCode)))
              .findFirst()
              .orElse(null);

  public RoleResolveResult resolveRole(
      IntygUser user,
      List<PersonInformation> personInfo,
      String defaultRole,
      UserCredentials userCredentials) {
    Assert.notNull(user, "Argument 'user' cannot be null");

    return lookupUserRole(user, personInfo, defaultRole, userCredentials);
  }

  /**
   * Get all configured (known/loaded) intygstyper.
   *
   * @return a list with intygstyper
   */
  public List<String> getIntygstyper() {
    return configurationLoader.getAuthoritiesConfiguration().getKnownIntygstyper();
  }

  /**
   * Get all configured (loaded) privileges.
   *
   * @return a list with privileges
   */
  public List<Privilege> getPrivileges() {
    return configurationLoader.getAuthoritiesConfiguration().getPrivileges();
  }

  public Role getRole(String name) {
    return fnRole.apply(name);
  }

  public RequestOrigin getRequestOrigin(String name) {
    return fnRequestOrigin.apply(name);
  }

  /**
   * Get all configured (loaded) request origins.
   *
   * @return a list with request origins
   */
  public List<RequestOrigin> getRequestOrigins() {
    return configurationLoader.getAuthoritiesConfiguration().getRequestOrigins();
  }

  /**
   * Get all configured (loaded) roles.
   *
   * @return a list with roles
   */
  public List<Role> getRoles() {
    return configurationLoader.getAuthoritiesConfiguration().getRoles();
  }

  /**
   * Get all configured (loaded) titles (a.k.a legitimerade yrkesgrupper).
   *
   * @return a list with titles
   */
  public List<Title> getTitles() {
    return configurationLoader.getAuthoritiesConfiguration().getTitles();
  }

  /**
   * Get all configured (loaded) title codes (a.k.a befattningskoder).
   *
   * @return a list with title codes
   */
  public List<TitleCode> getTitleCodes() {
    return configurationLoader.getAuthoritiesConfiguration().getTitleCodes();
  }

  /**
   * Gets all the features active for the user with active hsaIds.
   *
   * @param hsaIds the active hsaIds (vardenhet and vardgivare)
   * @return the map of all the features
   */
  public Map<String, Feature> getFeatures(List<String> hsaIds) {
    List<Feature> featureList =
        configurationLoader.getFeaturesConfiguration().getFeatures().stream()
            .map(Feature::new)
            .collect(Collectors.toList());

    List<Pilot> pilots =
        configurationLoader.getFeaturesConfiguration().getPilots().stream()
            .filter(p -> p.getHsaIds().stream().anyMatch(hsaIds::contains))
            .collect(Collectors.toList());

    handleActivatedPilots(featureList, pilots);
    handleDeactivatedPilots(featureList, pilots);
    return toMap(featureList, Feature::getName);
  }

  public SecurityConfigurationLoader getConfigurationLoader() {
    return configurationLoader;
  }

  public void setConfigurationLoader(SecurityConfigurationLoader configurationLoader) {
    this.configurationLoader = configurationLoader;
  }

  /**
   * Resolve a user role using SAML credential and HSA information.
   *
   * <p>Please note that the title attribute is not taken into account anymore, see INTYG-2627
   *
   * @return the resolved role
   */
  RoleResolveResult lookupUserRole(
      IntygUser user,
      List<PersonInformation> personInfo,
      String defaultRole,
      UserCredentials userCredentials) {
    RoleResolveResult roleResolveResult;
    List<String> legitimeradeYrkesgrupper =
        new HsaAttributeExtractor().extractLegitimeradeYrkesgrupper(personInfo);

    // 1. Bestäm användarens roll utefter legitimerade yrkesgrupper som hämtas från HSA.

    roleResolveResult = lookupUserRoleByLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    if (roleResolveResult != null) {
      return roleResolveResult;
    }

    // 2. Bestäm användarens roll utefter enbart befattningskod som kommer från SAML.
    roleResolveResult = lookupUserRoleByBefattningskod(getBefattningsKoder(user));
    if (roleResolveResult != null) {
      return roleResolveResult;
    }

    // 3. Bestäm användarens roll utefter kombinationen befattningskod och gruppförskrivarkod
    List<String> allaForskrivarKoder = buildAllaForskrivarKoderList(user, userCredentials);
    List<String> allaBefattningar = buildAllaBefattningarList(user, userCredentials);

    roleResolveResult =
        lookupUserRoleByBefattningskodAndGruppforskrivarkod(allaBefattningar, allaForskrivarKoder);
    if (roleResolveResult != null) {
      return roleResolveResult;
    }

    roleResolveResult = lookupUserRoleNurseOrMidWife(legitimeradeYrkesgrupper, defaultRole);
    if (roleResolveResult != null) {
      return roleResolveResult;
    }

    // 4. Användaren skall få fallback-rollen, t.ex. en vårdadministratör eller rehabkoordinator
    // inom landstinget.
    Role role = fnRole.apply(defaultRole);
    return new RoleResolveResult(role, defaultRole);
  }

  RoleResolveResult lookupUserRoleNurseOrMidWife(
      List<String> legitimeradeYrkesgrupper, String defaultRole) {
    if (legitimeradeYrkesgrupper.contains(AuthoritiesConstants.TITLE_BARNMORSKA)
        && !defaultRole.equals(AuthoritiesConstants.ROLE_KOORDINATOR)) {
      return new RoleResolveResult(
          fnRole.apply(AuthoritiesConstants.ROLE_BARNMORSKA),
          AuthoritiesConstants.TITLE_BARNMORSKA);
    }
    if (legitimeradeYrkesgrupper.contains(AuthoritiesConstants.TITLE_SJUKSKOTERSKA)
        && !defaultRole.equals(AuthoritiesConstants.ROLE_KOORDINATOR)) {
      return new RoleResolveResult(
          fnRole.apply(AuthoritiesConstants.ROLE_SJUKSKOTERSKA),
          AuthoritiesConstants.TITLE_SJUKSKOTERSKA);
    }
    return null;
  }

  private List<String> buildAllaForskrivarKoderList(
      IntygUser user, UserCredentials userCredentials) {
    List<String> allaForskrivarKoder = new ArrayList<>();
    if (user.getForskrivarkod() != null) {
      allaForskrivarKoder.add(user.getForskrivarkod());
    }
    allaForskrivarKoder.addAll(userCredentials.getGroupPrescriptionCode());
    return allaForskrivarKoder;
  }

  private List<String> buildAllaBefattningarList(IntygUser user, UserCredentials userCredentials) {
    List<String> allaBefattningar = new ArrayList<>();
    allaBefattningar.addAll(getBefattningsKoder(user));
    allaBefattningar.addAll(userCredentials.getPaTitleCode());
    return allaBefattningar;
  }

  private List<String> getBefattningsKoder(IntygUser user) {
    if (user.getBefattningsKoder().isEmpty()) {
      return user.getBefattningar();
    }
    return user.getBefattningsKoder().stream().map(PaTitle::getPaTitleCode).toList();
  }

  /**
   * Lookup user role by looking into 'legitimerade yrkesgrupper'. Currently there are only two
   * 'yrkesgrupper' to look for:
   *
   * <ul>
   *   <li>Läkare
   *   <li>Tandläkare
   * </ul>
   *
   * @param legitimeradeYrkesgrupper string array with 'legitimerade yrkesgrupper'
   * @return a user role if valid 'yrkesgrupper', otherwise null
   */
  RoleResolveResult lookupUserRoleByLegitimeradeYrkesgrupper(
      List<String> legitimeradeYrkesgrupper) {
    if (legitimeradeYrkesgrupper == null || legitimeradeYrkesgrupper.isEmpty()) {
      return null;
    }

    if (legitimeradeYrkesgrupper.contains(AuthoritiesConstants.TITLE_LAKARE)) {
      return new RoleResolveResult(
          fnRole.apply(AuthoritiesConstants.ROLE_LAKARE), AuthoritiesConstants.TITLE_LAKARE);
    }

    if (legitimeradeYrkesgrupper.contains(AuthoritiesConstants.TITLE_TANDLAKARE)) {
      return new RoleResolveResult(
          fnRole.apply(AuthoritiesConstants.ROLE_TANDLAKARE),
          AuthoritiesConstants.TITLE_TANDLAKARE);
    }

    return null;
  }

  RoleResolveResult lookupUserRoleByBefattningskod(List<String> befattningsKoder) {
    LOG.debug("  * befattningskod");

    if (befattningsKoder == null || befattningsKoder.size() == 0) {
      return null;
    }

    if (befattningsKoder.contains(TITLECODE_AT_LAKARE)
        || befattningsKoder.contains(TITLECODE_BT_LAKARE)) {
      // Look up roleTypeName from titleCode
      final var roleTypeName =
          getTitleCodes().stream()
              .filter(
                  tc ->
                      tc.getTitleCode().equals(TITLECODE_AT_LAKARE)
                          || tc.getTitleCode().equals(TITLECODE_BT_LAKARE))
              .filter(tc -> befattningsKoder.contains(tc.getTitleCode()))
              .findFirst()
              .map(TitleCode::getRoleTypeName)
              .orElse(AuthoritiesConstants.TITLE_LAKARE);
      return new RoleResolveResult(fnRole.apply(AuthoritiesConstants.ROLE_LAKARE), roleTypeName);
    }

    return null;
  }

  RoleResolveResult lookupUserRoleByBefattningskodAndGruppforskrivarkod(
      List<String> befattningsKoder, List<String> gruppforskrivarKoder) {
    for (String befattningskod : befattningsKoder) {
      for (String gruppforskrivarKod : gruppforskrivarKoder) {
        RoleResolveResult roleResolveResult =
            lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningskod, gruppforskrivarKod);
        if (roleResolveResult != null) {
          return roleResolveResult;
        }
      }
    }

    return null;
  }

  RoleResolveResult lookupUserRoleByBefattningskodAndGruppforskrivarkod(
      String befattningsKod, String gruppforskrivarKod) {
    if (befattningsKod == null || gruppforskrivarKod == null) {
      return null;
    }

    TitleCode titleCode = fnTitleCode.apply(befattningsKod, gruppforskrivarKod);
    if (titleCode == null) {
      return null;
    }

    Role role = fnRole.apply(titleCode.getRole().getName());
    if (role == null) {
      throw new AuthoritiesException(
          "fnRole.apply(titleCode.fnRole()) returnerade 'null' vilket indikerar felaktig konfiguration av roller");
    }

    return new RoleResolveResult(role, titleCode.getRoleTypeName());
  }

  private Predicate<RequestOrigin> isRequestOrigin(String name) {
    return ro -> ro.getName() != null && ro.getName().equalsIgnoreCase(name);
  }

  private Predicate<Role> isRole(String name) {
    return r -> r.getName() != null && r.getName().equalsIgnoreCase(name);
  }

  private Predicate<TitleCode> isTitleCode(String titleCode) {
    return tc -> tc.getTitleCode() != null && tc.getTitleCode().equalsIgnoreCase(titleCode);
  }

  private Predicate<TitleCode> isGroupPrescriptionCode(String groupPrescriptionCode) {
    return tc ->
        tc.getGroupPrescriptionCode() != null
            && tc.getGroupPrescriptionCode().equalsIgnoreCase(groupPrescriptionCode);
  }

  private void handleActivatedPilots(List<Feature> featureList, List<Pilot> pilots) {
    for (Feature feature : getFeatures(pilots, Pilot::getActivated)) {
      Optional<Feature> existing = getExisting(featureList, feature);
      if (existing.isPresent()) {
        Feature existingFeature = existing.get();
        existingFeature.setGlobal(existingFeature.getGlobal() || feature.getGlobal());
        existingFeature.setIntygstyper(
            union(existingFeature.getIntygstyper(), feature.getIntygstyper()));
      } else {
        featureList.add(feature);
      }
    }
  }

  private void handleDeactivatedPilots(List<Feature> featureList, List<Pilot> pilots) {
    for (Feature feature : getFeatures(pilots, Pilot::getDeactivated)) {
      getExisting(featureList, feature)
          .ifPresent(
              f -> {
                // In a deactivated feature in a pilot we remove all functionality in the pilot.
                // This means that if global == true in deactivated pilot then global == false in
                // the actual resulting feature for the
                // logged in user.
                f.setGlobal(f.getGlobal() && !feature.getGlobal());
                f.getIntygstyper().removeAll(feature.getIntygstyper());
              });
    }
  }

  private List<Feature> getFeatures(List<Pilot> pilots, Function<Pilot, List<Feature>> fun) {
    return pilots.stream()
        .map(fun)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private Optional<Feature> getExisting(List<Feature> featureList, Feature feature) {
    return featureList.stream()
        .filter(f -> Objects.equals(f.getName(), feature.getName()))
        .findFirst();
  }

  private List<String> union(List<String> listOne, List<String> listTwo) {
    Set<String> set = new HashSet<>(listOne);
    set.addAll(listTwo);
    return new ArrayList<>(set);
  }
}
