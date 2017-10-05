/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleApiControllerTest {

    private static final String DYNAMIC_LINK_PLACEHOLDER = "<LINK:";

    private static final String MODULE_ID_1 = "intygType1";
    private static final String MODULE_ID_2 = "intygType2";
    private static final String MODULE_ID_3 = "intygType3";

    private static final String MODULE_1_DETAILED_DESC = "This is a detailed description";
    private static final String SOME_REPLACED_DESCRIPTION = "Some replaced description";


    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private WebcertFeatureService featureService;

    @Mock
    private DynamicLinkService dynamicLinkService;

    @InjectMocks
    private ModuleApiController moduleApiController;

    @Before
    public void setup() {
        when(moduleRegistry.listAllModules()).thenReturn(Arrays.asList(new IntygModule(MODULE_ID_1, null, null, MODULE_1_DETAILED_DESC, null, null, null, null),
                new IntygModule(MODULE_ID_2, null, null, null, null, null, null, null),
                new IntygModule(MODULE_ID_3, null, null, null, null, null, null, null)));

        when(dynamicLinkService.apply(DYNAMIC_LINK_PLACEHOLDER, MODULE_1_DETAILED_DESC)).thenReturn(SOME_REPLACED_DESCRIPTION);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetModuleMap() {
        Response response = moduleApiController.getModulesMap();
        assertNotNull(response);
        List<IntygModule> res = (List<IntygModule>) response.getEntity();
        assertEquals(3, res.size());
        assertEquals(MODULE_ID_1, res.get(0).getId());
        assertEquals(MODULE_ID_2, res.get(1).getId());
        assertEquals(MODULE_ID_3, res.get(2).getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetActiveModules() {
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_1)).thenReturn(true);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_2)).thenReturn(true);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_3)).thenReturn(true);

        Response response = moduleApiController.getActiveModules();
        assertNotNull(response);
        List<IntygModule> res = (List<IntygModule>) response.getEntity();
        assertEquals(3, res.size());
        assertEquals(MODULE_ID_1, res.get(0).getId());
        assertEquals(MODULE_ID_2, res.get(1).getId());
        assertEquals(MODULE_ID_3, res.get(2).getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetActiveModulesOnlyReturnsActiveModules() {
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_1)).thenReturn(true);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_2)).thenReturn(false);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_3)).thenReturn(true);

        Response response = moduleApiController.getActiveModules();
        assertNotNull(response);
        List<IntygModule> res = (List<IntygModule>) response.getEntity();
        assertEquals(2, res.size());
        assertEquals(MODULE_ID_1, res.get(0).getId());
        assertEquals(MODULE_ID_3, res.get(1).getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetActiveModulesNoActive() {
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_1)).thenReturn(false);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_2)).thenReturn(false);
        when(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE_ID_3)).thenReturn(false);

        Response response = moduleApiController.getActiveModules();
        assertNotNull(response);
        List<IntygModule> res = (List<IntygModule>) response.getEntity();
        assertTrue(res.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testModuleDetailedDescriptionReplaced() {
        Response response = moduleApiController.getModulesMap();
        assertNotNull(response);
        List<IntygModule> res = (List<IntygModule>) response.getEntity();
        assertEquals(3, res.size());
        assertEquals(SOME_REPLACED_DESCRIPTION, res.get(0).getDetailedDescription());
    }
}
