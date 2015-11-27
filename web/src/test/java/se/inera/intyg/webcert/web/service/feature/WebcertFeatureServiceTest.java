package se.inera.intyg.webcert.web.service.feature;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;

@RunWith(MockitoJUnitRunner.class)
public class WebcertFeatureServiceTest {

    private static final String MODULE1 = "m1";
    private static final String MODULE2 = "m2";

    @Mock
    private IntygModuleRegistry mockModuleRegistry;

    @Mock
    private Environment mockEnv;

    @InjectMocks
    private WebcertFeatureServiceImpl featureService;

    private Map<String, Boolean> module1Features = new HashMap<>();
    private ModuleEntryPoint module1EntryPoint = mock(ModuleEntryPoint.class);

    private Map<String, Boolean> module2Features = new HashMap<>();
    private ModuleEntryPoint module2EntryPoint = mock(ModuleEntryPoint.class);

    @Before
    public void setupModuleMaps() {

        module1Features.put(ModuleFeature.HANTERA_FRAGOR.getName(), Boolean.TRUE);
        module1Features.put(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), Boolean.TRUE);

        module2Features.put(ModuleFeature.HANTERA_FRAGOR.getName(), Boolean.TRUE);
    }

    @Before
    public void setupExpectations() {
        when(mockModuleRegistry.getModuleEntryPoints()).thenReturn(Arrays.asList(module1EntryPoint, module2EntryPoint));
        when(module1EntryPoint.getModuleId()).thenReturn(MODULE1);
        when(module2EntryPoint.getModuleId()).thenReturn(MODULE2);
        when(module1EntryPoint.getModuleFeatures()).thenReturn(module1Features);
        when(module2EntryPoint.getModuleFeatures()).thenReturn(module2Features);
    }

    @Test
    public void testInitWebcertFeatureMap() {
        Map<String, Boolean> featuresMap = new HashMap<>();
        featureService.initWebcertFeatures(featuresMap);
        assertFalse(featuresMap.isEmpty());
        assertEquals(8, featuresMap.size());
    }

    @Test
    public void testInitModuleFeaturesMap() {
        Map<String, Boolean> featuresMap = new HashMap<>();
        featureService.initModuleFeatures(featuresMap);

        assertFalse(featuresMap.isEmpty());
        assertEquals(12, featuresMap.size());

        assertTrue(featuresMap.get(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE1)));
        assertTrue(featuresMap.get(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE2)));

        assertFalse(featuresMap.get(makeModuleName(ModuleFeature.MAKULERA_INTYG, MODULE1)));
        assertFalse(featuresMap.get(makeModuleName(ModuleFeature.HANTERA_INTYGSUTKAST, MODULE2)));

        verify(mockModuleRegistry).getModuleEntryPoints();
        verify(module1EntryPoint).getModuleFeatures();
        verify(module2EntryPoint).getModuleFeatures();
    }

    private String makeModuleName(ModuleFeature moduleFeature, String moduleName) {
        return StringUtils.join(new String[] { moduleFeature.getName(), moduleName }, ".");
    }

    @Test
    public void testProcessWebcertAndModelFeatures() {

        Map<String, Boolean> featuresMap = new HashMap<>();
        featuresMap.put(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), Boolean.FALSE);
        featuresMap.put(WebcertFeature.HANTERA_FRAGOR.getName(), Boolean.FALSE);
        featuresMap.put(WebcertFeature.MAKULERA_INTYG.getName(), Boolean.TRUE);
        featuresMap.put(WebcertFeature.KOPIERA_INTYG.getName(), Boolean.FALSE);

        Properties featureProps = new Properties();
        featureProps.setProperty(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), "true");
        featureProps.setProperty(WebcertFeature.HANTERA_FRAGOR.getName(), "true");
        featureProps.setProperty(WebcertFeature.MAKULERA_INTYG.getName(), "false");

        featureService.processWebcertAndModuleFeatureProperties(featureProps, featuresMap);

        assertTrue(featuresMap.get(WebcertFeature.HANTERA_INTYGSUTKAST.getName()));
        assertTrue(featuresMap.get(WebcertFeature.HANTERA_FRAGOR.getName()));
        assertFalse(featuresMap.get(WebcertFeature.MAKULERA_INTYG.getName()));
        assertFalse(featuresMap.get(WebcertFeature.KOPIERA_INTYG.getName()));
    }

    @Test
    public void testIsFeatureActive() {

        Properties featureProps = new Properties();
        featureProps.setProperty(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), "true");
        featureProps.setProperty(WebcertFeature.HANTERA_FRAGOR.getName(), "true");
        featureProps.setProperty(WebcertFeature.MAKULERA_INTYG.getName(), "false");
        featureProps.setProperty(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE1), "false");
        featureProps.setProperty(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE2), "true");

        featureService.setFeatures(featureProps);
        featureService.initFeaturesMap();

        assertEquals(20, featureService.getFeaturesMap().size());

        assertTrue(featureService.isFeatureActive(WebcertFeature.HANTERA_INTYGSUTKAST.getName()));
        assertTrue(featureService.isFeatureActive(WebcertFeature.HANTERA_INTYGSUTKAST));
        assertTrue(featureService.isFeatureActive(WebcertFeature.HANTERA_FRAGOR.getName()));
        assertTrue(featureService.isFeatureActive(WebcertFeature.HANTERA_FRAGOR));
        assertFalse(featureService.isFeatureActive(WebcertFeature.MAKULERA_INTYG.getName()));
        assertFalse(featureService.isFeatureActive(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE1)));
        assertTrue(featureService.isFeatureActive(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE2)));
    }

    @Test
    public void testEnvVariableShouldOverrideProperty() {

        Properties featureProps = new Properties();
        featureProps.setProperty(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), "true");

        when(mockEnv.getProperty(WebcertFeature.HANTERA_INTYGSUTKAST.getName())).thenReturn("false");

        featureService.setFeatures(featureProps);
        featureService.initFeaturesMap();

        // This should be overridden by env prop
        assertFalse(featureService.isFeatureActive(WebcertFeature.HANTERA_INTYGSUTKAST.getName()));

    }

    @Test
    public void testGetActiveFeatures() {

        Properties featureProps = new Properties();
        featureProps.setProperty(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), "true");
        featureProps.setProperty(WebcertFeature.HANTERA_FRAGOR.getName(), "true");
        featureProps.setProperty(WebcertFeature.MAKULERA_INTYG.getName(), "false");
        featureProps.setProperty(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE1), "false");
        featureProps.setProperty(makeModuleName(ModuleFeature.HANTERA_FRAGOR, MODULE2), "true");

        featureService.setFeatures(featureProps);
        featureService.initFeaturesMap();

        Set<String> res = featureService.getActiveFeatures();

        assertThat(res, contains("hanteraFragor", "hanteraFragor.m2", "hanteraIntygsutkast", "hanteraIntygsutkast.m1"));
    }

}
