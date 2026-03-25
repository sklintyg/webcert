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

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.infra.integration.hsatk.model.HsaSystemRole;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation.PaTitle;
import se.inera.intyg.infra.integration.hsatk.model.legacy.UserCredentials;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.util.HsaAttributeExtractor;
import se.inera.intyg.webcert.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;

/**
 * Provides a number of default implementations for decorating a IntygUser principal with various
 * information extracted from HSA models.
 *
 * <p>Created by eriklupander on 2016-05-17.
 */
public class DefaultUserDetailsDecorator {

  private static final String SPACE = " ";
  private static final Logger LOG = LoggerFactory.getLogger(DefaultUserDetailsDecorator.class);

  private HsaAttributeExtractor hsaAttributeExtractor = new HsaAttributeExtractor();

  public void decorateIntygUserWithAdditionalInfo(
      IntygUser intygUser, List<PersonInformation> hsaPersonInfo) {

    List<String> specialiseringar = hsaAttributeExtractor.extractSpecialiseringar(hsaPersonInfo);
    List<String> legitimeradeYrkesgrupper =
        hsaAttributeExtractor.extractLegitimeradeYrkesgrupper(hsaPersonInfo);
    List<String> befattningar = hsaAttributeExtractor.extractBefattningar(hsaPersonInfo);
    List<PaTitle> befattningskoder = hsaAttributeExtractor.extractBefattningsKoder(hsaPersonInfo);
    String titel = hsaAttributeExtractor.extractTitel(hsaPersonInfo);

    intygUser.setSpecialiseringar(specialiseringar);
    intygUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    intygUser.setBefattningar(befattningar);
    intygUser.setBefattningsKoder(befattningskoder);
    intygUser.setTitel(titel);
  }

  public void decorateIntygUserWithAuthenticationMethod(
      IntygUser intygUser, String authenticationScheme) {

    if (authenticationScheme.endsWith(":fake")) {
      intygUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
    } else {
      intygUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
    }
  }

  public void decorateIntygUserWithDefaultVardenhet(IntygUser intygUser) {
    setFirstVardenhetOnFirstVardgivareAsDefault(intygUser);
    LOG.debug(
        "Setting care unit '{}' as default unit on user '{}'",
        intygUser.getValdVardenhet().getId(),
        intygUser.getHsaId());
  }

  public void decorateIntygUserWithSystemRoles(
      IntygUser intygUser, UserCredentials userCredentials) {
    if (userCredentials != null && userCredentials.getHsaSystemRole() != null) {
      intygUser.setSystemRoles(
          userCredentials.getHsaSystemRole().stream()
              .map(DefaultUserDetailsDecorator::hsaSystemRoleAsString)
              .collect(Collectors.toList()));
    }
  }

  public String compileName(String fornamn, String mellanOchEfterNamn) {

    StringBuilder sb = new StringBuilder();

    if (fornamn != null && !fornamn.isEmpty()) {
      sb.append(fornamn);
    }

    if (mellanOchEfterNamn != null && !mellanOchEfterNamn.isEmpty()) {
      if (!sb.isEmpty()) {
        sb.append(SPACE);
      }
      sb.append(mellanOchEfterNamn);
    }

    return sb.toString();
  }

  private boolean setFirstVardenhetOnFirstVardgivareAsDefault(IntygUser intygUser) {
    for (Vardgivare vg : intygUser.getVardgivare()) {
      if (!vg.getVardenheter().isEmpty()) {
        intygUser.setValdVardgivare(vg);
        intygUser.setValdVardenhet(vg.getVardenheter().get(0));
        return true;
      }
    }
    return false;
  }

  private static String hsaSystemRoleAsString(HsaSystemRole systemRole) {
    if (systemRole.getSystemId() == null || systemRole.getSystemId().trim().isEmpty()) {
      return systemRole.getRole();
    } else {
      return systemRole.getSystemId() + ";" + systemRole.getRole();
    }
  }
}
