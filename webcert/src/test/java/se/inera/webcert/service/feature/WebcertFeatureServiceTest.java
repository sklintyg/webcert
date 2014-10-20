package se.inera.webcert.service.feature;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.webcert.modules.IntygModuleRegistry;

@RunWith(MockitoJUnitRunner.class)
public class WebcertFeatureServiceTest {
    
    private static final String MODULE1 = "m1";
    private static final String MODULE2 = "m2";
    
    @Mock
    private IntygModuleRegistry mockModuleRegistry;
    
    @InjectMocks
    private WebcertFeatureServiceImpl featureService;
    
    private Map<String, Boolean> module1Features = new HashMap<String, Boolean>();
    private ModuleEntryPoint module1EntryPoint = mock(ModuleEntryPoint.class);
    
    private Map<String, Boolean> module2Features = new HashMap<String, Boolean>();
    private ModuleEntryPoint module2EntryPoint = mock(ModuleEntryPoint.class);

    @Before
    public void setup() {
        Properties props = new Properties();
        props.setProperty(Features.HANTERA_INTYGSUTKAST.getName(), "true");
        props.setProperty(Features.HANTERA_FRAGOR.getName(), "true");
        props.setProperty(Features.MAKULERA_INTYG.getName(), "false");

        featureService.setFeatures(props);
        featureService.initWebcertFeaturesMap();
    }
    
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
    public void testFeatureMapIsNotNull() {
        assertNotNull(featureService.getWebcertFeaturesMap());
    }

    @Test
    public void testIsFeatureActive() {
        assertTrue(featureService.isFeatureActive(Features.HANTERA_INTYGSUTKAST.getName()));
        assertFalse(featureService.isFeatureActive(Features.KOPIERA_INTYG.getName()));
    }
    
    @Test
    public void testGetActiveFeatures() {
        
        Set<String> res = featureService.getActiveFeatures();
        
        assertThat(res, contains("hanteraFragor", "hanteraIntygsutkast"));
    }

    @Test
    public void testInitModuleFeaturesMap() {
        
        featureService.initModuleFeaturesMap();
        
        assertNotNull(featureService.getModuleFeaturesMap());
        
        assertTrue(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE1));
        assertTrue(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR.getName(), MODULE1));
        assertTrue(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR.getName(), MODULE2));
        
        assertFalse(featureService.isModuleFeatureActive(ModuleFeature.MAKULERA_INTYG.getName(), MODULE1));
        assertFalse(featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), MODULE2));
        
        Set<String> res = featureService.getActiveModuleFeatures();
        assertThat(res, contains("hanteraFragor.m1", "hanteraFragor.m2", "hanteraIntygsutkast.m1"));
        
        verify(mockModuleRegistry).getModuleEntryPoints();
        verify(module1EntryPoint).getModuleFeatures();
        verify(module2EntryPoint).getModuleFeatures();
    }
    
}
