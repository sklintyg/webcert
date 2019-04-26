/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.access.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;

import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

abstract public class AccessServiceTestToolkit {

    static WebCertUser createUserWithUtkastAuthority(String intygsTyp) {
        List<String> featureNames = Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        final Map<String, Feature> featureMap = getFeatureMap(intygsTyp, featureNames);
        final Map<String, Privilege> privilegeMap = getUtkastAuthority(intygsTyp);
        return createUserWithoutParameters(intygsTyp, featureMap, privilegeMap, false);
    }

    static WebCertUser createUserWithoutUtkastAuthority(String intygsTyp) {
        List<String> featureNames = Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        final Map<String, Feature> featureMap = getFeatureMap(intygsTyp, featureNames);
        return createUserWithoutParameters(intygsTyp, featureMap, new HashMap<>(), false);
    }

    static WebCertUser createUserWithoutUtkastFeature(String intygsTyp) {
        final Map<String, Privilege> privilegeMap = getUtkastAuthority(intygsTyp);
        return createUserWithoutParameters(intygsTyp, new HashMap<>(), privilegeMap, false);
    }

    public static WebCertUser createUser(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        return createUserWithoutParameters(intygsTyp,
                getFeatureMap(intygsTyp, accessServiceTestData.getFeatures()),
                getPrivilegesMap(intygsTyp, accessServiceTestData.getPrivileges()), false);
    }

    public static WebCertUser createUserDifferentUnit(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        return createUserWithoutParameters(intygsTyp,
                getFeatureMap(intygsTyp, accessServiceTestData.getFeatures()),
                getPrivilegesMap(intygsTyp, accessServiceTestData.getPrivileges()), true);
    }

    public static WebCertUser createUser(String intygsTyp, AccessServiceTestData accessServiceTestData, boolean inactiveUnit) {
        return createUser(intygsTyp, accessServiceTestData, inactiveUnit, true);
    }

    public static WebCertUser createUser(String intygsTyp, AccessServiceTestData accessServiceTestData, boolean inactiveUnit,
            boolean renewOk) {
        return createUser(intygsTyp, accessServiceTestData, inactiveUnit, renewOk, false);
    }

    public static WebCertUser createUser(String intygsTyp, AccessServiceTestData accessServiceTestData, boolean inactiveUnit,
            boolean renewOk, boolean differentUnit) {
        return createUserWithParameters(intygsTyp,
                getFeatureMap(intygsTyp, accessServiceTestData.getFeatures()),
                getPrivilegesMap(intygsTyp, accessServiceTestData.getPrivileges()),
                inactiveUnit,
                renewOk,
                differentUnit);
    }

    private static WebCertUser createUserWithParameters(String intygsTyp, Map<String, Feature> featureMap,
            Map<String, Privilege> privilegesMap,
            boolean inactiveUnit,
            boolean fornyaOk,
            boolean differentUnit) {

        final WebCertUser webCertUser = mock(WebCertUser.class);

        doReturn(featureMap).when(webCertUser).getFeatures();
        doReturn(getRolesMap(AuthoritiesConstants.ROLE_LAKARE)).when(webCertUser).getRoles();
        doReturn(UserOriginType.NORMAL.name()).when(webCertUser).getOrigin();
        doReturn(privilegesMap).when(webCertUser).getAuthorities();
        doReturn(getParameters(inactiveUnit, fornyaOk)).when(webCertUser).getParameters();

        if (differentUnit) {
            doReturn(Arrays.asList(new String[] { "AnnatEnhetsId" })).when(webCertUser).getIdsOfSelectedVardenhet();
        } else {
            doReturn(Arrays.asList(new String[] { "EnhetsId" })).when(webCertUser).getIdsOfSelectedVardenhet();
        }

        return webCertUser;
    }

    private static WebCertUser createUserWithoutParameters(String intygsTyp, Map<String, Feature> featureMap,
            Map<String, Privilege> privilegesMap, boolean differentUnit) {

        final WebCertUser webCertUser = mock(WebCertUser.class);

        doReturn(featureMap).when(webCertUser).getFeatures();
        doReturn(getRolesMap(AuthoritiesConstants.ROLE_LAKARE)).when(webCertUser).getRoles();
        doReturn(UserOriginType.NORMAL.name()).when(webCertUser).getOrigin();
        doReturn(privilegesMap).when(webCertUser).getAuthorities();
        doReturn(null).when(webCertUser).getParameters();

        if (differentUnit) {
            doReturn(Arrays.asList(new String[] { "AnnatEnhetsId" })).when(webCertUser).getIdsOfSelectedVardenhet();
        } else {
            doReturn(Arrays.asList(new String[] { "EnhetsId" })).when(webCertUser).getIdsOfSelectedVardenhet();
        }

        return webCertUser;
    }

    private static Map<String, Privilege> getUtkastAuthority(String intygsTyp) {
        return getPrivilegesMap(
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                        Collections.emptyList(),
                        Lists.newArrayList(createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList(intygsTyp)))));
    }

    private static Map<String, Feature> getFeatureMap(String intygsTyp, List<String> featureNames) {
        Map<String, Feature> featureMap = new HashMap<>();

        for (String featureName : featureNames) {
            Feature feature = new Feature();
            feature.setName(featureName);
            feature.setIntygstyper(Collections.singletonList(intygsTyp));
            feature.setGlobal(true);
            featureMap.put(feature.getName(), feature);
        }

        return featureMap;
    }

    private static IntegrationParameters getParameters(boolean inactiveUnit, boolean fornyaOk) {
        return new IntegrationParameters("",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                false,
                false,
                inactiveUnit,
                fornyaOk);
    }

    private static Map<String, Role> getRolesMap(String roleName) {
        final HashMap<String, Role> rolesHashMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        rolesHashMap.put(roleName, role);
        return rolesHashMap;
    }

    private static Map<String, Privilege> getPrivilegesMap(String intygsTyp, List<String> privileges) {
        final HashMap<String, Privilege> privilegesHashMap = new HashMap<>();
        for (String privilege : privileges) {
            privilegesHashMap.put(privilege, createPrivilege(privilege, Collections.emptyList(),
                    Lists.newArrayList(createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList(intygsTyp)))));
        }
        return privilegesHashMap;
    }

    private static Map<String, Privilege> getPrivilegesMap(Privilege privilege) {
        final HashMap<String, Privilege> privilegesHashMap = new HashMap<>();
        privilegesHashMap.put(privilege.getName(), privilege);
        return privilegesHashMap;
    }

    private static WebCertUser createUser(String roleName, Privilege p, Map<String, Feature> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> privilegeHashMap = new HashMap<>();
        privilegeHashMap.put(p.getName(), p);
        user.setAuthorities(privilegeHashMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> roleHashMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        roleHashMap.put(roleName, role);

        user.setRoles(roleHashMap);
        return user;
    }

    private static RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(name);
        requestOrigin.setIntygstyper(intygstyper);
        return requestOrigin;
    }

    private static Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege privilege = new Privilege();
        privilege.setName(name);
        privilege.setIntygstyper(intygsTyper);
        privilege.setRequestOrigins(requestOrigins);
        return privilege;
    }

    public static PersonSvar getAlivePatient(Personnummer personnummer) {
        final PersonSvar personSvar = mock(PersonSvar.class);
        final Person person = mock(Person.class);

        doReturn(person).when(personSvar).getPerson();
        doReturn(false).when(person).isAvliden();
        doReturn(false).when(person).isSekretessmarkering();

        return personSvar;
    }

    public static PersonSvar getDiseasedPatient(Personnummer personnummer) {
        final PersonSvar personSvar = mock(PersonSvar.class);
        final Person person = mock(Person.class);

        doReturn(person).when(personSvar).getPerson();
        doReturn(true).when(person).isAvliden();
        doReturn(false).when(person).isSekretessmarkering();

        return personSvar;
    }

    public static Map<String, Map<String, PreviousIntyg>> createEmptyPreviousForUtkast(String intygsTyp) {
        final Map<String, PreviousIntyg> previousUtkastMap = new HashMap<>();

        final Map<String, PreviousIntyg> previousIntygMap = new HashMap<>();

        final Map<String, Map<String, PreviousIntyg>> previousIntygUtkastMap = new HashMap<>();
        previousIntygUtkastMap.put("utkast", previousUtkastMap);
        previousIntygUtkastMap.put("intyg", previousIntygMap);

        return previousIntygUtkastMap;
    }

    public static Map<String, Map<String, PreviousIntyg>> createPreviousUtkastForUtkast(String intygsTyp) {
        final PreviousIntyg previousIntyg = mock(PreviousIntyg.class);

        doReturn(true).when(previousIntyg).isSameVardgivare();

        final Map<String, PreviousIntyg> previousUtkastMap = new HashMap<>();
        previousUtkastMap.put(intygsTyp, previousIntyg);

        final Map<String, PreviousIntyg> previousIntygMap = new HashMap<>();

        final Map<String, Map<String, PreviousIntyg>> previousIntygUtkastMap = new HashMap<>();
        previousIntygUtkastMap.put("utkast", previousUtkastMap);
        previousIntygUtkastMap.put("intyg", previousIntygMap);

        return previousIntygUtkastMap;
    }

    public static Map<String, Map<String, PreviousIntyg>> createPreviousIntygForUtkast(String intygsTyp, boolean checkVardgivare) {
        final PreviousIntyg previousIntyg = mock(PreviousIntyg.class);

        if (checkVardgivare) {
            doReturn(true).when(previousIntyg).isSameVardgivare();
        }

        final Map<String, PreviousIntyg> previousUtkastMap = new HashMap<>();

        final Map<String, PreviousIntyg> previousIntygMap = new HashMap<>();
        previousIntygMap.put(intygsTyp, previousIntyg);

        final Map<String, Map<String, PreviousIntyg>> previousIntygUtkastMap = new HashMap<>();
        previousIntygUtkastMap.put("utkast", previousUtkastMap);
        previousIntygUtkastMap.put("intyg", previousIntygMap);

        return previousIntygUtkastMap;
    }
}
