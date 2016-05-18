package se.inera.intyg.webcert.web.auth.authorities;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationLoader;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Magnus Ekstrand on 2016-05-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesHelperTest {

    // Privilege
    public static final String SKRIVA_INTYG = "SKRIVA_INTYG";

    // Request origins
    public static final String NORMAL = "NORMAL";
    public static final String DJUPINTEGRATION = "DJUPINTEGRATION";
    public static final String UTHOPP = "UTHOPP";

    private final String configurationLocation = "AuthoritiesConfigurationLoaderTest/authorities-test.yaml";

    private AuthoritiesConfigurationLoader configurationLoader = new AuthoritiesConfigurationLoader(configurationLocation);
    private AuthoritiesResolver authoritiesResolver = new AuthoritiesResolver();

    @Mock
    private HsaPersonService hsaPersonService;

    @InjectMocks
    private AuthoritiesHelper authoritiesHelper = new AuthoritiesHelper(authoritiesResolver);

    @Before
    public void setup() throws Exception {
        configurationLoader.afterPropertiesSet();
        authoritiesResolver.setConfigurationLoader(configurationLoader);
    }

    // Kända intygstyper
    List<String> knownIntygstyper = Arrays.asList(new String[] { "fk7263", "ts-bas", "ts-diabetes" });


    @Test
    public void whenPrivilegeHasNoIntygstyperAndNoRequestOrigins() throws Exception {
        Privilege privilege = createPrivilege(SKRIVA_INTYG, new ArrayList<>(), new ArrayList<>());
        Set<String> intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(NORMAL, toMap(privilege)), SKRIVA_INTYG);

        assertTrue(intygstyper.size() == authoritiesResolver.getIntygstyper().size());
        assertTrue(intygstyper.containsAll(knownIntygstyper));
    }

    @Test
    public void whenPrivilegeHasIntygstyperButNoRequestOrigins() throws Exception {
        List<String> typer = knownIntygstyper.stream().limit(2).collect(Collectors.toList());
        Privilege privilege = createPrivilege(SKRIVA_INTYG, typer, new ArrayList<>());

        Set<String> intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(NORMAL, toMap(privilege)), SKRIVA_INTYG);

        assertTrue(intygstyper.size() == typer.size());
        assertTrue(intygstyper.containsAll(typer));
    }

    @Test
    public void whenPrivilegeHasNoIntygstyperButRequestOrigins() throws Exception {
        Set<String> intygstyper = null;
        WebCertUser user = null;

        Map<String, List<String>> intygstyperOrigins = new HashMap<>();
        intygstyperOrigins.put(NORMAL, knownIntygstyper);
        intygstyperOrigins.put(DJUPINTEGRATION, knownIntygstyper);
        intygstyperOrigins.put(UTHOPP, getIntygstyper("fk7263"));

        Privilege privilege = createPrivilege(SKRIVA_INTYG, new ArrayList<>(), createRequestOrigins(intygstyperOrigins));
        Map<String, Privilege> privilegeMap = toMap(privilege);

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 3);
        assertTrue(intygstyper.contains("fk7263"));
        assertTrue(intygstyper.contains("ts-bas"));
        assertTrue(intygstyper.contains("ts-diabetes"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 3);
        assertTrue(intygstyper.contains("fk7263"));
        assertTrue(intygstyper.contains("ts-bas"));
        assertTrue(intygstyper.contains("ts-diabetes"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 1);
        assertTrue(intygstyper.contains("fk7263"));
    }

    @Test
    public void whenPrivilegeHasIntygstyperAndRequestOrigins() throws Exception {
        Set<String> intygstyper = null;
        WebCertUser user = null;

        List<String> intygstyperPrivilege = knownIntygstyper.stream().filter(typ -> typ.equals("fk7263") || typ.equals("ts-bas")).collect(Collectors.toList());

        Map<String, List<String>> intygstyperOrigins = new HashMap<>();
        intygstyperOrigins.put(NORMAL, knownIntygstyper);
        intygstyperOrigins.put(DJUPINTEGRATION, knownIntygstyper);
        intygstyperOrigins.put(UTHOPP, getIntygstyper("fk7263"));

        Privilege privilege = createPrivilege(SKRIVA_INTYG, intygstyperPrivilege, createRequestOrigins(intygstyperOrigins));
        Map<String, Privilege> privilegeMap = toMap(privilege);

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 2);
        assertTrue(intygstyper.contains("fk7263"));
        assertTrue(intygstyper.contains("ts-bas"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 2);
        assertTrue(intygstyper.contains("fk7263"));
        assertTrue(intygstyper.contains("ts-bas"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 1);
        assertTrue(intygstyper.contains("fk7263"));
    }

    @Test
    public void whenPrivilegeHasNoIntygstyperButRequestOriginsAndWhereNormalImplicitlyAllowsAll() throws Exception {
        Set<String> intygstyper = null;

        Map<String, List<String>> intygstyperOrigins = new HashMap<>();
        intygstyperOrigins.put(NORMAL, new ArrayList<>());
        intygstyperOrigins.put(DJUPINTEGRATION, getIntygstyper("fk7263"));
        intygstyperOrigins.put(UTHOPP, getIntygstyper("ts-bas", "ts-diabetes"));

        Privilege privilege = createPrivilege(SKRIVA_INTYG, new ArrayList<>(), createRequestOrigins(intygstyperOrigins));
        Map<String, Privilege> privilegeMap = toMap(privilege);

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(NORMAL, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 3);
        assertTrue(intygstyper.contains("fk7263"));
        assertTrue(intygstyper.contains("ts-bas"));
        assertTrue(intygstyper.contains("ts-diabetes"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(DJUPINTEGRATION, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 1);
        assertTrue(intygstyper.contains("fk7263"));

        intygstyper = authoritiesHelper.getIntygstyperForPrivilege(createWebCertUser(UTHOPP, privilegeMap), SKRIVA_INTYG);
        assertTrue(intygstyper.size() == 2);
        assertTrue(intygstyper.contains("ts-bas"));
        assertTrue(intygstyper.contains("ts-diabetes"));
    }

    private WebCertUser createWebCertUser(String origin, Map<String, Privilege> privileges) {
        WebCertUser user = new WebCertUser();
        user.setOrigin(origin);
        user.setAuthorities(privileges);

        return user;
    }

    private Privilege createPrivilege(String name, List<String> intygstyper, List<RequestOrigin> requestOrigins) {
        Privilege privilege = new Privilege();
        privilege.setName(name);
        privilege.setIntygstyper(intygstyper);
        privilege.setRequestOrigins(requestOrigins);

        return privilege;
    }

    private List<RequestOrigin> createRequestOrigins(Map<String, List<String>> requestOrigins) {
        List<RequestOrigin> list = new ArrayList<>();
        requestOrigins.entrySet().stream().forEach(e -> list.add(createRequestOrigin(e.getKey(), e.getValue())));

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
        return knownIntygstyper.stream().filter(t -> list.contains(t)).collect(Collectors.toList());
    }

    private Map<String, Privilege> toMap(Privilege privilege) {
        return Collections.unmodifiableMap(Stream
                .of(new AbstractMap.SimpleEntry<>(privilege.getName(), privilege))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

}