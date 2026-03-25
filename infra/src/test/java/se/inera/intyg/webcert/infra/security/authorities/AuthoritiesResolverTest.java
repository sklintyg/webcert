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
package se.inera.intyg.webcert.infra.security.authorities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.infra.security.common.model.Role;
import se.inera.intyg.webcert.infra.security.common.model.RoleResolveResult;

@ExtendWith(MockitoExtension.class)
class AuthoritiesResolverTest {

  private static final String DEFAULT_ROLE = "defaultRole";
  private final String authoritiesConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
  private final String featuresConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/features-test.yaml";
  private final Integer defaultMaxAliasesForCollections = 300;

  @Spy
  private SecurityConfigurationLoader configurationLoader =
      new SecurityConfigurationLoader(
          authoritiesConfigurationLocation,
          featuresConfigurationLocation,
          defaultMaxAliasesForCollections);

  @InjectMocks
  private CommonAuthoritiesResolver authoritiesResolver = new CommonAuthoritiesResolver();

  @BeforeEach
  public void setup() {
    configurationLoader.afterPropertiesSet();
  }

  @Test
  void lookupUserRoleWhenTitleIsDoctor() {
    List<String> titles = Collections.singletonList("Läkare");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
    assertTrue(
        roleResolveResult.getRole().getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
  }

  @Test
  void lookupUserRoleWhenMultipleTitlesAndOneIsDoctor() {
    List<String> titles = Arrays.asList("Läkare", "Barnmorska", "Sjuksköterska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
    assertTrue(
        roleResolveResult.getRole().getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
  }

  @Test
  void lookupUserRoleWhenMultipleTitlesAndNoneExists() {
    List<String> titles = List.of("Smed", "Notarie");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
    assertNull(roleResolveResult);
  }

  @Test
  void lookupUserRoleWhenTitleCodeIs204010() {
    List<String> befattningsKoder = Collections.singletonList("204010");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
    assertTrue(
        roleResolveResult.getRole().getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    assertTrue(roleResolveResult.getRoleTypeName().equalsIgnoreCase("Läkare-204010"));
  }

  @Test
  void lookupUserRoleWhenTitleCodeIs203020() {
    List<String> befattningsKoder = Collections.singletonList("203020");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
    assertTrue(
        roleResolveResult.getRole().getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    assertTrue(roleResolveResult.getRoleTypeName().equalsIgnoreCase("Läkare-203020"));
  }

  @Test
  void lookupUserRoleWhenTitleCodes204010And203020() {
    List<String> befattningsKoder = Arrays.asList("203020", "204010");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
    assertTrue(
        roleResolveResult.getRole().getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
    assertTrue(roleResolveResult.getRoleTypeName().equalsIgnoreCase("Läkare-204010"));
  }

  @Test
  void lookupUserRoleWhenTitleCodeIsNot204010Or203020() {
    List<String> befattningsKoder = Arrays.asList("203090", "204090", "", null);
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskod(befattningsKoder);
    assertNull(roleResolveResult);
  }

  @Test
  void lookupUserRoleByTitleCodeAndGroupPrescriptionCode() {
    List<String> befattningsKoder = Arrays.asList("204010", "203020", "203090", "204090");
    List<String> gruppforskrivarKoder = Arrays.asList("9300005", "9100009");

    Role[][] roleMatrix = new Role[4][2];

    for (int i = 0; i < befattningsKoder.size(); i++) {
      for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
        RoleResolveResult roleResult =
            authoritiesResolver.lookupUserRoleByBefattningskodAndGruppforskrivarkod(
                befattningsKoder.get(i), gruppforskrivarKoder.get(j));
        roleMatrix[i][j] = roleResult != null ? roleResult.getRole() : null;
      }
    }

    /* Expected matrix:
       [0,0] null
       [0,1] null
       [1,0] null
       [1,1] null
       [2,0] LAKARE
       [2,1] null
       [3,0] null
       [3,1] LAKARE
    */

    for (int i = 0; i < befattningsKoder.size(); i++) {
      for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
        if (i == 0) {
          assertNull(roleMatrix[i][j]);
        } else if (i == 1) {
          assertNull(roleMatrix[i][j]);
        } else if ((i == 3) && (j == 0)) {
          assertNull(roleMatrix[i][j]);
        } else if ((i == 2) && (j == 1)) {
          assertNull(roleMatrix[i][j]);
        } else {
          assertTrue(roleMatrix[i][j].getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_LAKARE));
        }
      }
    }
  }

  @Test
  void lookupUserRoleByTitleCodeAndGroupPrescriptionCodeNoMatchReturnsNull() {
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskodAndGruppforskrivarkod(
            new ArrayList<>(), new ArrayList<>());

    assertNull(roleResolveResult);
  }

  @Test
  void lookupUserRoleByTitleCodeAndGroupPrescriptionCodeCombination() {
    List<String> befattningsKoder = Arrays.asList("204010", "203090", "204090");
    List<String> gruppforskrivarKoder = Arrays.asList("9300005", "9100009");

    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleByBefattningskodAndGruppforskrivarkod(
            befattningsKoder, gruppforskrivarKoder);

    // Assert        assertEquals(AuthoritiesConstants.ROLE_LAKARE,
    // roleResolveResult.getRole().getName());
    assertEquals("Läkare-203090-9300005", roleResolveResult.getRoleTypeName());
  }

  @Test
  void lookupUserRoleNurseWhenTitleIsNurse() {
    List<String> titles = List.of("Sjuksköterska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleNurseOrMidWife(titles, DEFAULT_ROLE);
    assertTrue(
        roleResolveResult
            .getRole()
            .getName()
            .equalsIgnoreCase(AuthoritiesConstants.ROLE_SJUKSKOTERSKA));
  }

  @Test
  void lookupUserRoleMidWifeWhenTitleIsMidWife() {
    List<String> titles = List.of("Barnmorska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleNurseOrMidWife(titles, DEFAULT_ROLE);
    assertTrue(
        roleResolveResult
            .getRole()
            .getName()
            .equalsIgnoreCase(AuthoritiesConstants.ROLE_BARNMORSKA));
  }

  @Test
  void lookupUserRoleNurseWhenTitleIsNurseAndDefaultRoleIsRehabkoordinator() {
    List<String> titles = List.of("Sjuksköterska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleNurseOrMidWife(
            titles, AuthoritiesConstants.ROLE_KOORDINATOR);
    assertNull(roleResolveResult);
  }

  @Test
  void lookupUserRoleNurseWhenMultipleTitlesAndOneIsNurse() {
    List<String> titles = List.of("Vårdadministratör", "Sjuksköterska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleNurseOrMidWife(titles, DEFAULT_ROLE);
    assertTrue(
        roleResolveResult
            .getRole()
            .getName()
            .equalsIgnoreCase(AuthoritiesConstants.ROLE_SJUKSKOTERSKA));
  }

  @Test
  void lookupUserRoleMidWifeWhenMultipleTitlesAndOneIsNurse() {
    List<String> titles = List.of("Barnmorska", "Sjuksköterska");
    RoleResolveResult roleResolveResult =
        authoritiesResolver.lookupUserRoleNurseOrMidWife(titles, DEFAULT_ROLE);
    assertTrue(
        roleResolveResult
            .getRole()
            .getName()
            .equalsIgnoreCase(AuthoritiesConstants.ROLE_BARNMORSKA));
  }

  @Test
  void testGetFeatures() {
    Map<String, Feature> features = authoritiesResolver.getFeatures(new ArrayList<>());

    // Sanity check for features which should always be active by default
    assertTrue(features.containsKey(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));
    assertTrue(features.containsKey(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR));
    assertTrue(features.containsKey(AuthoritiesConstants.FEATURE_SKICKA_INTYG));

    assertFalse(features.containsKey("non-existing-feature"));
    assertFalse(features.containsKey(""));
    assertFalse(features.containsKey(null));

    assertEquals(
        9, features.get(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).getIntygstyper().size());

    Map<String, Feature> featuresNonexistingHsaId =
        authoritiesResolver.getFeatures(List.of("non-existing"));

    // Sanity check for features which should always be active by default
    assertTrue(
        featuresNonexistingHsaId.containsKey(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));
    assertTrue(featuresNonexistingHsaId.containsKey(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR));
    assertTrue(featuresNonexistingHsaId.containsKey(AuthoritiesConstants.FEATURE_SKICKA_INTYG));

    assertFalse(featuresNonexistingHsaId.containsKey("non-existing-feature"));
    assertFalse(featuresNonexistingHsaId.containsKey(""));
    assertFalse(featuresNonexistingHsaId.containsKey(null));

    assertEquals(
        9,
        featuresNonexistingHsaId
            .get(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .getIntygstyper()
            .size());
  }

  @Test
  void testGetFeaturesAdditiveFeatures() {
    Map<String, Feature> features = authoritiesResolver.getFeatures(List.of("additive"));
    assertTrue(features.get(AuthoritiesConstants.FEATURE_SRS).getGlobal());
    assertEquals(
        Arrays.asList("fk7263", "lisjp"),
        features.get(AuthoritiesConstants.FEATURE_SRS).getIntygstyper());
  }

  @Test
  void testGetFeaturesSubtractingFeatures() {
    Map<String, Feature> features = authoritiesResolver.getFeatures(List.of("subtractive"));
    assertFalse(features.get(AuthoritiesConstants.FEATURE_SRS).getGlobal());
    assertFalse(features.get(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT).getGlobal());
    assertFalse(
        features
            .get(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT)
            .getIntygstyper()
            .contains("db"));
  }

  @Test
  void testGetFeaturesBoth() {
    Map<String, Feature> features = authoritiesResolver.getFeatures(List.of("both"));
    assertTrue(features.get(AuthoritiesConstants.FEATURE_SRS).getGlobal());
    assertFalse(features.get(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT).getGlobal());
    assertFalse(
        features
            .get(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT)
            .getIntygstyper()
            .contains("db"));
  }

  @Test
  void testGetFeaturesBoth2() {
    Map<String, Feature> features = authoritiesResolver.getFeatures(List.of("both2"));
    assertFalse(features.get(AuthoritiesConstants.FEATURE_SRS).getGlobal());
    List<String> expected = Arrays.asList("lisjp", "db", "doi");
    Collections.sort(expected);
    List<String> actual = features.get(AuthoritiesConstants.FEATURE_SRS).getIntygstyper();
    Collections.sort(actual);
    assertEquals(expected, actual);
  }
}
