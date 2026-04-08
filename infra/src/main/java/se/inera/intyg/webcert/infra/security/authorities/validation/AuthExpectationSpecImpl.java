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
package se.inera.intyg.webcert.infra.security.authorities.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.infra.security.common.model.Role;
import se.inera.intyg.webcert.infra.security.common.model.UserDetails;
import se.inera.intyg.webcert.infra.security.common.model.UserOriginType;

/** Created by marced on 18/12/15. */
public class AuthExpectationSpecImpl implements AuthExpectationSpecification {

  /*
   * Instance context state
   */
  private UserDetails user;
  private Optional<String> intygsTypeContext;

  /*
   * Constraints states
   */
  private Optional<String[]> featureConstraints = Optional.empty();
  private Optional<String[]> featureNotConstraints = Optional.empty();

  private Optional<UserOriginType[]> originConstraints = Optional.empty();
  private Optional<UserOriginType[]> originNotConstraints = Optional.empty();

  private Optional<String[]> roleConstraints = Optional.empty();
  private Optional<String[]> roleNotConstraints = Optional.empty();

  private List<Optional<String>> privilegeConstraints = new ArrayList<>();
  private List<Optional<String>> privilegeNotConstraints = new ArrayList<>();

  private List<String> errors = new ArrayList<>();

  public AuthExpectationSpecImpl(UserDetails user, Optional<String> intygstyp) {
    this.user = user;
    this.intygsTypeContext = intygstyp;
  }

  @Override
  public boolean isVerified() {
    errors.clear();

    if (featureConstraints.isPresent()
        && Arrays.stream(featureConstraints.get()).noneMatch(this::checkHasFeature)) {
      errors.add(
          formatFeatureError(
              featureConstraints.get(),
              "mandatory features '%s' was not present in users features."));
    }

    if (featureNotConstraints.isPresent()
        && Arrays.stream(featureNotConstraints.get()).anyMatch(this::checkHasFeature)) {
      errors.add(
          formatFeatureError(
              featureNotConstraints.get(),
              "forbidden features '%s' was present in users features."));
    }

    if (originConstraints.isPresent()
        && Arrays.stream(originConstraints.get()).noneMatch(oc -> this.checkHasOrigin(oc.name()))) {
      errors.add(
          String.format(
              "mandatory origins '%s' did not match users origin value of '%s'.",
              Arrays.stream(originConstraints.get())
                  .map(UserOriginType::name)
                  .collect(Collectors.joining(",")),
              this.user.getOrigin()));
    }

    if (originNotConstraints.isPresent()
        && Arrays.stream(originNotConstraints.get())
            .anyMatch(oc -> this.checkHasOrigin(oc.name()))) {
      errors.add(
          String.format(
              "forbidden features '%s' matched user orgin value '%s'",
              Arrays.stream(originNotConstraints.get())
                  .map(UserOriginType::name)
                  .collect(Collectors.joining(",")),
              this.user.getOrigin()));
    }

    if (roleConstraints.isPresent()
        && Arrays.stream(roleConstraints.get()).noneMatch(rc -> this.checkRole(rc))) {
      errors.add(
          String.format(
              "mandatory roles '%s' was not present in users roles.",
              String.join(",", roleConstraints.get())));
    }

    if (roleNotConstraints.isPresent()
        && Arrays.stream(roleNotConstraints.get()).anyMatch(rc -> this.checkRole(rc))) {
      errors.add(
          String.format(
              "forbidden roles '%s' was present in users roles.",
              String.join(",", roleNotConstraints.get())));
    }

    for (Optional<String> privilegeConstraint : privilegeConstraints) {
      if (privilegeConstraint.isPresent() && !checkHasPrivilege(privilegeConstraint.get())) {
        errors.add(
            formatPrivilegeError(
                "user does not have mandatory privilege '%s'", privilegeConstraint.get()));
      }
    }

    for (Optional<String> privilegeNotConstraint : privilegeNotConstraints) {
      if (privilegeNotConstraint.isPresent() && checkHasPrivilege(privilegeNotConstraint.get())) {
        errors.add(
            formatPrivilegeError(
                "forbidden privilege '%s' was present in user privileges",
                privilegeNotConstraint.get()));
      }
    }

    return errors.isEmpty();
  }

  private String formatPrivilegeError(String start, String privilegeConstraint) {
    StringBuilder sb = new StringBuilder(String.format(start, privilegeConstraint));
    sb.append(" (");
    if (intygsTypeContext.isPresent()) {
      sb.append("intygType = ");
      sb.append(intygsTypeContext.get());
      sb.append(", ");
    }
    sb.append("origin = ");
    sb.append(user.getOrigin());
    if (user.getRoles() != null) {
      sb.append(", roles = '");
      sb.append(
          user.getRoles().values().stream().map(Role::getName).collect(Collectors.joining(", ")));
      sb.append('\'');
    }
    sb.append(").");

    return sb.toString();
  }

  private String formatFeatureError(String[] features, String format) {
    return String.format(
        format,
        Arrays.stream(features)
            .map(
                s ->
                    intygsTypeContext.isPresent()
                        ? String.format("%s.%s", s, intygsTypeContext.get())
                        : s)
            .collect(Collectors.joining(",")));
  }

  @Override
  public void orThrow() {
    if (!isVerified()) {
      throw new AuthoritiesException(
          "Authorization Validation failed because " + String.join(", ", errors));
    }
  }

  @Override
  public String error() {
    return errors.isEmpty()
        ? ""
        : "Authorization Validation failed because " + String.join(", ", errors);
  }

  @Override
  public void orThrow(RuntimeException exception) {
    if (!isVerified()) {
      throw exception;
    }
  }

  @Override
  public AuthExpectationSpecification features(String... featureConstraints) {
    this.featureConstraints = Optional.of(featureConstraints);
    return this;
  }

  @Override
  public AuthExpectationSpecification notFeatures(String... invalidFeatureConstraints) {
    this.featureNotConstraints = Optional.of(invalidFeatureConstraints);
    return this;
  }

  private boolean checkHasFeature(String featureName) {
    Feature feature = user.getFeatures().get(featureName);
    if (feature == null) {
      return false;
    } else if (intygsTypeContext.isPresent()) {
      return feature.getGlobal() && feature.getIntygstyper().contains(intygsTypeContext.get());
    } else {
      return feature.getGlobal();
    }
  }

  @Override
  public AuthExpectationSpecification origins(UserOriginType... validOriginTypes) {
    this.originConstraints = Optional.of(validOriginTypes);
    return this;
  }

  @Override
  public AuthExpectationSpecification notOrigins(UserOriginType... invalidOriginTypes) {
    this.originNotConstraints = Optional.of(invalidOriginTypes);
    return this;
  }

  private boolean checkHasOrigin(String originConstraint) {
    return this.user.getOrigin() != null && this.user.getOrigin().equals(originConstraint);
  }

  @Override
  public AuthExpectationSpecification roles(String... validRoles) {
    this.roleConstraints = Optional.of(validRoles);
    return this;
  }

  @Override
  public AuthExpectationSpecification notRoles(String... invalidRoles) {
    this.roleNotConstraints = Optional.of(invalidRoles);
    return this;
  }

  private boolean checkRole(String role) {
    return this.user.getRoles() != null && this.user.getRoles().containsKey(role);
  }

  @Override
  public AuthExpectationSpecification privilege(String privilegeConstraint) {
    this.privilegeConstraints.add(Optional.of(privilegeConstraint));
    return this;
  }

  @Override
  public AuthExpectationSpecification privilegeIf(String privilegeConstraint, boolean evaluate) {
    if (evaluate) {
      this.privilegeConstraints.add(Optional.of(privilegeConstraint));
    }
    return this;
  }

  @Override
  public AuthExpectationSpecification notPrivilege(String privilegeNotConstraint) {
    this.privilegeNotConstraints.add(Optional.of(privilegeNotConstraint));
    return this;
  }

  @Override
  public AuthExpectationSpecification notPrivilegeIf(
      String privilegeNotConstraint, boolean evaluate) {
    if (evaluate) {
      this.privilegeNotConstraints.add(Optional.of(privilegeNotConstraint));
    }
    return this;
  }

  private boolean checkHasPrivilege(String privilegeConstraint) {
    final Privilege privilegeConfig =
        this.user.getAuthorities() != null
            ? this.user.getAuthorities().get(privilegeConstraint)
            : null;

    if (privilegeConfig == null) {
      return false;
    }

    if (this.intygsTypeContext.isPresent()
        && privilegeConfig.getIntygstyper().size() > 0
        && privilegeConfig.getIntygstyper().stream()
            .noneMatch(t -> t.equals(this.intygsTypeContext.get()))) {
      return false;
    }

    // Does the previlege have requestOrigin constraint(s)?
    if (privilegeConfig.getRequestOrigins().size() > 0) {
      // The users origin must match one of them
      Optional<RequestOrigin> matchingOriginConfig =
          privilegeConfig.getRequestOrigins().stream()
              .filter(ro -> ro.getName().equals(this.user.getOrigin()))
              .findAny();
      if (!matchingOriginConfig.isPresent()) {
        return false;
      }

      // If the originConfig has a intygstypeConstraint - one of them must also match any given
      // intygstype
      // context..
      if (this.intygsTypeContext.isPresent()
          && matchingOriginConfig.get().getIntygstyper().size() > 0
          && matchingOriginConfig.get().getIntygstyper().stream()
              .noneMatch(t -> t.equals(this.intygsTypeContext.get()))) {
        return false;
      }
    }

    // we passed all checks, so privilege is granted
    return true;
  }
}
