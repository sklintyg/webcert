/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate;

import org.junit.Before;
import org.mockito.Mock;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidatorTest {

    protected static final String FK7263 = Fk7263EntryPoint.MODULE_ID;
    protected static final String TSBAS = TsBasEntryPoint.MODULE_ID;
    protected static final String LUSE = LuseEntryPoint.MODULE_ID;

    private static List<String> ALL_INTYG_TYPES = Arrays.asList(Fk7263EntryPoint.MODULE_ID,
            TsBasEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID,
            LisjpEntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID,
            LuaenaEntryPoint.MODULE_ID, DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID);
    protected WebCertUser user;

    @Mock
    protected IntygModuleRegistry moduleRegistry;

    @Mock
    protected PatientDetailsResolver patientDetailsResolver;

    @Mock
    protected AuthoritiesHelper authoritiesHelper;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, FK7263.toLowerCase())).thenReturn(true);
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, TSBAS.toLowerCase())).thenReturn(true);
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, LUSE.toLowerCase())).thenReturn(true);

        when(authoritiesHelper.getIntygstyperAllowedForAvliden())
                .thenReturn(Arrays.asList(DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID));
        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
                .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
        when(moduleRegistry.moduleExists(Fk7263EntryPoint.MODULE_ID)).thenReturn(true);
        when(moduleRegistry.moduleExists(TsBasEntryPoint.MODULE_ID)).thenReturn(true);
        when(moduleRegistry.moduleExists(LuseEntryPoint.MODULE_ID)).thenReturn(true);

        when(moduleRegistry.getIntygModule(eq(Fk7263EntryPoint.MODULE_ID))).thenReturn(buildIntygModule(FK7263, true));
        when(moduleRegistry.getIntygModule(eq(TsBasEntryPoint.MODULE_ID))).thenReturn(buildIntygModule(TSBAS, false));
        when(moduleRegistry.getIntygModule(eq(LuseEntryPoint.MODULE_ID))).thenReturn(buildIntygModule(LUSE, false));

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(new HashSet<>(ALL_INTYG_TYPES));

        user = buildUser();
    }

    private IntygModule buildIntygModule(String id, boolean deprecated) {
        return new IntygModule(id, "", "", "", "", "", "", "", "", deprecated);
    }

    protected WebCertUser buildUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        Feature feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        feature.setIntygstyper(Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID));
        user.setFeatures(Collections.singletonMap(feature.getName(), feature));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        return user;
    }

    protected WebCertUser buildUserUnauthorized() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, ALL_INTYG_TYPES));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, ALL_INTYG_TYPES));
        Feature feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        feature.setIntygstyper(Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID,
                LisjpEntryPoint.MODULE_ID, LuaenaEntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID,
                DbModuleEntryPoint.MODULE_ID, DoiModuleEntryPoint.MODULE_ID));
        user.setFeatures(Collections.singletonMap(feature.getName(), feature));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        return user;
    }

    protected Privilege createPrivilege(String privilege) {
        return createPrivilege(privilege, Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID));
    }

    protected Privilege createPrivilege(String privilege, List<String> intygstyper) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(UserOriginType.DJUPINTEGRATION.name());
        requestOrigin.setIntygstyper(intygstyper);
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(intygstyper);
        return priv;
    }
}
