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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.infra.security.common.model.RequestOrigin;

/**
 * @author Magnus Ekstrand on 2016-05-13.
 */
@ExtendWith(MockitoExtension.class)
class AuthoritiesHelperTest {

  // Privilege
  public static final String SKRIVA_INTYG = "SKRIVA_INTYG";

  // Request origins
  public static final String NORMAL = "NORMAL";
  public static final String DJUPINTEGRATION = "DJUPINTEGRATION";
  public static final String UTHOPP = "UTHOPP";

  private final String authoritiesConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/authorities-test.yaml";
  private final String featuresConfigurationLocation =
      "classpath:AuthoritiesConfigurationLoaderTest/features-test.yaml";
  private final Integer defaultMaxAliasesForCollections = 300;

  private final SecurityConfigurationLoader configurationLoader =
      new SecurityConfigurationLoader(
          authoritiesConfigurationLocation,
          featuresConfigurationLocation,
          defaultMaxAliasesForCollections);
  private final CommonAuthoritiesResolver authoritiesResolver = new CommonAuthoritiesResolver();

  @Mock private HsaPersonService hsaPersonService;

  @InjectMocks
  private AuthoritiesHelper authoritiesHelper = new AuthoritiesHelper(authoritiesResolver);

  @BeforeEach
  void setup() {
    configurationLoader.afterPropertiesSet();
    authoritiesResolver.setConfigurationLoader(configurationLoader);
  }

  // Kända intygstyper
  private final List<String> knownIntygstyper = Arrays.asList("fk7263", "ts-bas", "ts-diabetes");

  @Test
  void whenPrivilegeHasNoIntygstyperAndNoRequestOrigins() {
    Privilege privilege = createPrivilege(SKRIVA_INTYG, new ArrayList<>(), new ArrayList<>());
    Set<String> intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(NORMAL, toMap(privilege)), SKRIVA_INTYG);

    assertEquals(intygstyper.size(), authoritiesResolver.getIntygstyper().size());
    assertTrue(intygstyper.containsAll(knownIntygstyper));
  }

  @Test
  void whenPrivilegeHasIntygstyperButNoRequestOrigins() {
    List<String> typer = knownIntygstyper.stream().limit(2).toList();
    Privilege privilege = createPrivilege(SKRIVA_INTYG, typer, new ArrayList<>());

    Set<String> intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(NORMAL, toMap(privilege)), SKRIVA_INTYG);

    assertEquals(intygstyper.size(), typer.size());
    assertTrue(intygstyper.containsAll(typer));
  }

  @Test
  void whenPrivilegeHasNoIntygstyperButRequestOrigins() {
    Set<String> intygstyper;

    Map<String, List<String>> intygstyperOrigins = new HashMap<>();
    intygstyperOrigins.put(NORMAL, knownIntygstyper);
    intygstyperOrigins.put(DJUPINTEGRATION, knownIntygstyper);
    intygstyperOrigins.put(UTHOPP, getIntygstyper("fk7263"));

    Privilege privilege =
        createPrivilege(SKRIVA_INTYG, new ArrayList<>(), createRequestOrigins(intygstyperOrigins));
    Map<String, Privilege> privilegeMap = toMap(privilege);

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
    assertEquals(3, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
    assertTrue(intygstyper.contains("ts-bas"));
    assertTrue(intygstyper.contains("ts-diabetes"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
    assertEquals(3, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
    assertTrue(intygstyper.contains("ts-bas"));
    assertTrue(intygstyper.contains("ts-diabetes"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
    assertEquals(1, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
  }

  @Test
  void whenPrivilegeHasIntygstyperAndRequestOrigins() {
    Set<String> intygstyper;

    List<String> intygstyperPrivilege =
        knownIntygstyper.stream()
            .filter(typ -> typ.equals("fk7263") || typ.equals("ts-bas"))
            .toList();

    Map<String, List<String>> intygstyperOrigins = new HashMap<>();
    intygstyperOrigins.put(NORMAL, knownIntygstyper);
    intygstyperOrigins.put(DJUPINTEGRATION, knownIntygstyper);
    intygstyperOrigins.put(UTHOPP, getIntygstyper("fk7263"));

    Privilege privilege =
        createPrivilege(
            SKRIVA_INTYG, intygstyperPrivilege, createRequestOrigins(intygstyperOrigins));
    Map<String, Privilege> privilegeMap = toMap(privilege);

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
    assertEquals(2, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
    assertTrue(intygstyper.contains("ts-bas"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
    assertEquals(2, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
    assertTrue(intygstyper.contains("ts-bas"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
    assertEquals(1, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
  }

  @Test
  void whenPrivilegeHasNoIntygstyperButRequestOriginsAndWhereNormalImplicitlyAllowsAll() {
    Set<String> intygstyper;

    Map<String, List<String>> intygstyperOrigins = new HashMap<>();
    intygstyperOrigins.put(NORMAL, new ArrayList<>());
    intygstyperOrigins.put(DJUPINTEGRATION, getIntygstyper("fk7263"));
    intygstyperOrigins.put(UTHOPP, getIntygstyper("ts-bas", "ts-diabetes"));

    Privilege privilege =
        createPrivilege(SKRIVA_INTYG, new ArrayList<>(), createRequestOrigins(intygstyperOrigins));
    Map<String, Privilege> privilegeMap = toMap(privilege);

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
    assertEquals(3, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));
    assertTrue(intygstyper.contains("ts-bas"));
    assertTrue(intygstyper.contains("ts-diabetes"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
    assertEquals(1, intygstyper.size());
    assertTrue(intygstyper.contains("fk7263"));

    intygstyper =
        authoritiesHelper.getIntygstyperForPrivilege(
            createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
    assertEquals(2, intygstyper.size());
    assertTrue(intygstyper.contains("ts-bas"));
    assertTrue(intygstyper.contains("ts-diabetes"));
  }

  private IntygUser createWebCertUser(String origin, Map<String, Privilege> privileges) {
    IntygUser user = new IntygUser("hsa-id");
    user.setOrigin(origin);
    user.setAuthorities(privileges);

    return user;
  }

  private Privilege createPrivilege(
      String name, List<String> intygstyper, List<RequestOrigin> requestOrigins) {
    Privilege privilege = new Privilege();
    privilege.setName(name);
    privilege.setIntygstyper(intygstyper);
    privilege.setRequestOrigins(requestOrigins);

    return privilege;
  }

  private List<RequestOrigin> createRequestOrigins(Map<String, List<String>> requestOrigins) {
    List<RequestOrigin> list = new ArrayList<>();
    requestOrigins.entrySet().forEach(e -> list.add(createRequestOrigin(e.getKey(), e.getValue())));

    return list;
  }

  private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
    RequestOrigin requestOrigin = new RequestOrigin();
    requestOrigin.setName(name);
    requestOrigin.setIntygstyper(intygstyper);

    return requestOrigin;
  }

  private List<String> getIntygstyper(String... intygstyper) {
    List<String> list = Arrays.asList(intygstyper);
    return knownIntygstyper.stream().filter(list::contains).toList();
  }

  private Map<String, Privilege> toMap(Privilege privilege) {
    HashMap<String, Privilege> map = new HashMap<>();
    map.put(privilege.getName(), privilege);
    return map;
  }
}
