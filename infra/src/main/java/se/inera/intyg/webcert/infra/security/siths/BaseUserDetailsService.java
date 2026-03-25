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
package se.inera.intyg.webcert.infra.security.siths;

import static se.inera.intyg.webcert.infra.security.authorities.AuthoritiesResolverUtil.toMap;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.UserAuthorizationInfo;
import se.inera.intyg.infra.integration.hsatk.model.legacy.UserCredentials;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.infra.security.common.model.UserOrigin;
import se.inera.intyg.webcert.infra.security.common.service.AuthenticationLogger;
import se.inera.intyg.webcert.infra.security.exception.HsaServiceException;
import se.inera.intyg.webcert.infra.security.exception.MissingHsaEmployeeInformation;
import se.inera.intyg.webcert.infra.security.exception.MissingMedarbetaruppdragException;

@Slf4j
public abstract class BaseUserDetailsService {

  protected abstract String getDefaultRole();

  protected abstract HttpServletRequest getCurrentRequest();

  @Autowired(required = false)
  private Optional<UserOrigin> userOrigin;

  @Autowired private HsaOrganizationsService hsaOrganizationsService;
  @Autowired private HsaPersonService hsaPersonService;
  @Autowired private AuthenticationLogger monitoringLogService;

  @Autowired
  public void setCommonAuthoritiesResolver(CommonAuthoritiesResolver commonAuthoritiesResolver) {
    this.commonAuthoritiesResolver = commonAuthoritiesResolver;
  }

  protected CommonAuthoritiesResolver commonAuthoritiesResolver;
  private final DefaultUserDetailsDecorator defaultUserDetailsDecorator =
      new DefaultUserDetailsDecorator();

  protected IntygUser buildUserPrincipal(String employeeHsaId, String authenticationScheme) {
    log.debug("Creating user object...");

    final var personInfo = getPersonInfo(employeeHsaId);
    final var userAuthorizationInfo = getAuthorizedVardgivare(employeeHsaId);

    try {
      assertEmployee(employeeHsaId, personInfo);
      assertAuthorizedVardgivare(employeeHsaId, userAuthorizationInfo.getVardgivare());
      final var intygUser =
          createIntygUser(employeeHsaId, authenticationScheme, userAuthorizationInfo, personInfo);

      // Clean out förskrivarkod
      intygUser.setForskrivarkod("0000000");
      return intygUser;

    } catch (MissingMedarbetaruppdragException e) {
      monitoringLogService.logMissingMedarbetarUppdrag(employeeHsaId);
      log.error("Missing medarbetaruppdrag. This needs to be fixed!!!");
      throw e;
    }
  }

  /**
   * Fetches a list of {@link Vardgivare} from HSA (over NTjP) that the specified employeeHsaId has
   * medarbetaruppdrag "Vård och behandling" for. Uses
   * infrastructure:directory:authorizationmanagement:GetCredentialsForPersonIncludingProtectedPerson.
   *
   * <p>Override to provide your own mechanism for fetching Vardgivare.
   */
  protected UserAuthorizationInfo getAuthorizedVardgivare(String employeeHsaId) {
    log.debug("Retrieving authorized units from HSA...");

    try {
      return hsaOrganizationsService.getAuthorizedEnheterForHosPerson(employeeHsaId);

    } catch (Exception e) {
      log.error(
          "Failure retrieving authorized units from HSA for user {}, error message {}",
          employeeHsaId,
          e.getMessage());
      throw new HsaServiceException(employeeHsaId, e);
    }
  }

  /**
   * Fetches a list of PersonInformationType from HSA using
   * infrastructure:directory:employee:GetEmployeeIncludingProtectedPerson.
   *
   * <p>Override to provide your own implementation for fetching PersonInfo.
   */
  protected List<PersonInformation> getPersonInfo(String employeeHsaId) {
    log.debug("Retrieving user information from HSA...");

    List<PersonInformation> hsaPersonInfo;
    try {
      hsaPersonInfo = hsaPersonService.getHsaPersonInfo(employeeHsaId);
      if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
        log.info(
            "Call to web service getHsaPersonInfo did not return any info for user '{}'",
            employeeHsaId);
      }

    } catch (Exception e) {
      log.error(
          "Failed retrieving user information from HSA for user {}, error message {}",
          employeeHsaId,
          e.getMessage());
      throw new HsaServiceException(employeeHsaId, e);
    }
    return hsaPersonInfo;
  }

  protected void assertAuthorizedVardgivare(
      String employeeHsaId, List<Vardgivare> authorizedVardgivare) {
    log.debug("Assert user has authorization to one or more 'vårdenheter'");

    if (authorizedVardgivare == null || authorizedVardgivare.isEmpty()) {
      throw new MissingMedarbetaruppdragException(employeeHsaId);
    }
  }

  /**
   * Creates the base {@link IntygUser} instance that implementing subclasses then can decorate on
   * their own. Optionally, all the decorate* methods can be individually overridden by implementing
   * subclasses.
   *
   * @param employeeHsaId hsaId for the authorizing user. From SAML ticket.
   * @param authenticationScheme auth scheme, i.e. what auth method used, typically :siths or :fake
   * @param userAuthorizationInfo UserCredentials and List of vardgivare fetched from HSA, each
   *     entry is actually a tree of vardgivare -> vardenhet(er) -> mottagning(ar) where the user
   *     has medarbetaruppdrag 'Vård och Behandling'.
   * @param personInfo Employee information from HSA.
   * @return A base IntygUser Principal.
   */
  protected IntygUser createIntygUser(
      String employeeHsaId,
      String authenticationScheme,
      UserAuthorizationInfo userAuthorizationInfo,
      List<PersonInformation> personInfo) {
    log.debug("Decorate/populate user object with additional information");

    final var intygUser = new IntygUser(employeeHsaId);
    decorateIntygUserWithBasicInfo(
        intygUser, userAuthorizationInfo, personInfo, authenticationScheme);
    decorateIntygUserWithAdditionalInfo(intygUser, personInfo);
    decorateIntygUserWithAuthenticationMethod(intygUser, authenticationScheme);
    decorateIntygUserWithRoleAndAuthorities(
        intygUser, personInfo, userAuthorizationInfo.getUserCredentials());
    decorateIntygUserWithSystemRoles(intygUser, userAuthorizationInfo.getUserCredentials());
    decorateIntygUserWithDefaultVardenhet(intygUser);
    decorateIntygUserWithAvailableFeatures(intygUser);
    return intygUser;
  }

  protected void decorateIntygUserWithAdditionalInfo(
      IntygUser intygUser, List<PersonInformation> hsaPersonInfo) {
    defaultUserDetailsDecorator.decorateIntygUserWithAdditionalInfo(intygUser, hsaPersonInfo);
  }

  protected void decorateIntygUserWithAuthenticationMethod(
      IntygUser intygUser, String authenticationScheme) {
    defaultUserDetailsDecorator.decorateIntygUserWithAuthenticationMethod(
        intygUser, authenticationScheme);
  }

  protected void decorateIntygUserWithSystemRoles(
      IntygUser intygUser, UserCredentials userCredentials) {
    defaultUserDetailsDecorator.decorateIntygUserWithSystemRoles(intygUser, userCredentials);
  }

  protected void decorateIntygUserWithDefaultVardenhet(IntygUser intygUser) {
    defaultUserDetailsDecorator.decorateIntygUserWithDefaultVardenhet(intygUser);
  }

  public void decorateIntygUserWithAvailableFeatures(IntygUser intygUser) {
    List<String> hsaIds = new ArrayList<>();
    if (intygUser.getValdVardenhet() != null) {
      hsaIds.add(intygUser.getValdVardenhet().getId());
    }
    if (intygUser.getValdVardgivare() != null) {
      hsaIds.add(intygUser.getValdVardgivare().getId());
    }

    intygUser.setFeatures(commonAuthoritiesResolver.getFeatures(hsaIds));
  }

  protected String compileName(String fornamn, String mellanOchEfterNamn) {
    return defaultUserDetailsDecorator.compileName(fornamn, mellanOchEfterNamn);
  }

  // Allow subclasses to use HSA services.
  protected HsaOrganizationsService getHsaOrganizationsService() {
    return hsaOrganizationsService;
  }

  // Allow subclasses to use HSA services.
  protected HsaPersonService getHsaPersonService() {
    return hsaPersonService;
  }

  private void decorateIntygUserWithBasicInfo(
      IntygUser intygUser,
      UserAuthorizationInfo userAuthorizationInfo,
      List<PersonInformation> personInfo,
      String authenticationScheme) {
    intygUser.setFornamn(personInfo.getFirst().getGivenName());
    intygUser.setEfternamn(personInfo.getFirst().getMiddleAndSurName());
    intygUser.setNamn(
        compileName(personInfo.getFirst().getGivenName(), personInfo.get(0).getMiddleAndSurName()));
    intygUser.setVardgivare(userAuthorizationInfo.getVardgivare());
    intygUser.setSekretessMarkerad(
        personInfo.stream()
            .anyMatch(pi -> pi.getProtectedPerson() != null && pi.getProtectedPerson()));

    // Förskrivarkod is sensitive information so make sure it is overwritten after role resolution.
    intygUser.setForskrivarkod(
        userAuthorizationInfo.getUserCredentials().getPersonalPrescriptionCode());

    intygUser.setAuthenticationScheme(authenticationScheme);

    userOrigin.ifPresent(
        origin ->
            intygUser.setOrigin(
                commonAuthoritiesResolver
                    .getRequestOrigin(origin.resolveOrigin(getCurrentRequest()))
                    .getName()));

    // Set commission names per enhetsId (required for PDL logging)
    intygUser.setMiuNamnPerEnhetsId(userAuthorizationInfo.getCommissionNamePerCareUnit());
  }

  protected void decorateIntygUserWithRoleAndAuthorities(
      IntygUser intygUser, List<PersonInformation> personInfo, UserCredentials userCredentials) {
    final var roleResolveResult =
        commonAuthoritiesResolver.resolveRole(
            intygUser, personInfo, getDefaultRole(), userCredentials);
    log.debug("User role is set to {}", roleResolveResult.getRole());

    intygUser.setRoles(toMap(roleResolveResult.getRole()));
    intygUser.setRoleTypeName(roleResolveResult.getRoleTypeName());
    intygUser.setAuthorities(
        toMap(roleResolveResult.getRole().getPrivileges(), Privilege::getName));
  }

  private void assertEmployee(String employeeHsaId, List<PersonInformation> personInfo) {
    if (personInfo == null || personInfo.isEmpty()) {
      log.error(
          "Cannot authorize user with employeeHsaId '{}', no records found for Employee in HoSP.",
          employeeHsaId);
      throw new MissingHsaEmployeeInformation(employeeHsaId);
    }
  }
}
