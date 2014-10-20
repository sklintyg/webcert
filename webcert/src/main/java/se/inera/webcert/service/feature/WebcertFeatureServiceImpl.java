package se.inera.webcert.service.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.webcert.modules.IntygModuleRegistry;

@Service
public class WebcertFeatureServiceImpl implements WebcertFeatureService {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertFeatureService.class);
    
    private static final String COMMA_SEP = ", ";
    private static final String DOT_SEP = ".";

    @Autowired
    private IntygModuleRegistry mouleRegistry;

    @Autowired
    @Qualifier("webcertFeatures")
    private Properties features;

    private Map<String, Boolean> webcertFeaturesMap = new HashMap<String, Boolean>();

    private Map<String, Boolean> moduleFeaturesMap = new HashMap<String, Boolean>();

    @PostConstruct
    public void initFeaturesMap() {
        initWebcertFeaturesMap();
        initModuleFeaturesMap();
        LOG.info("Active Webcert features is: {}", StringUtils.join(getActiveWebcertFeatures(), COMMA_SEP));
        LOG.info("Active Webcert module features is: {}", StringUtils.join(getActiveModuleFeatures(), COMMA_SEP));
    }

    public void initWebcertFeaturesMap() {
        for (Features feature : Features.values()) {

            Boolean featureState = Boolean.parseBoolean(features.getProperty(feature.getName()));

            if (featureState == null) {
                webcertFeaturesMap.put(feature.getName(), Boolean.FALSE);
                continue;
            }

            webcertFeaturesMap.put(feature.getName(), featureState);
        }
    }

    public void initModuleFeaturesMap() {

        String moduleId;
        Map<String, Boolean> moduleMap;

        for (ModuleEntryPoint mep : mouleRegistry.getModuleEntryPoints()) {

            moduleId = mep.getModuleId();
            moduleMap = mep.getModuleFeatures();
            
            if (moduleMap == null || moduleMap.isEmpty()) {
                LOG.warn("Module {} did not expose any features! All features this of module will be disabled!", moduleId);
                moduleMap = Collections.emptyMap();
            }

            String key;
            String moduleFeatureName;
            Boolean moduleFeatureState = Boolean.FALSE;

            for (ModuleFeature moduleFeature : ModuleFeature.values()) {
                moduleFeatureName = moduleFeature.getName();
                moduleFeatureState = (moduleMap.get(moduleFeatureName) != null) ? moduleMap.get(moduleFeatureName) : Boolean.FALSE;
                key = StringUtils.join(new String[] { moduleFeatureName, moduleId }, DOT_SEP);
                moduleFeaturesMap.put(key, moduleFeatureState);
            }
        }

    }

    public boolean isFeatureActive(Features feature) {
        return isFeatureActive(feature.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.feature.WebcertFeatureService#isFeatureActive(java.lang.String)
     */
    @Override
    public boolean isFeatureActive(String featureName) {
        Boolean featureState = webcertFeaturesMap.get(featureName);
        return (featureState != null) ? featureState.booleanValue() : false;
    }

    public boolean isModuleFeatureActive(String featureName, String moduleName) {
        if (isFeatureActive(featureName)) {
            String key = StringUtils.join(new String[] { featureName, moduleName }, DOT_SEP);
            Boolean moduleFeatureState = moduleFeaturesMap.get(key);
            return (moduleFeatureState != null) ? moduleFeatureState.booleanValue() : false;
        }

        return false;
    }


    public Set<String> getActiveWebcertFeatures() {
        return getActiveFeaturesFromMap(getWebcertFeaturesMap());
    }

    public Set<String> getActiveModuleFeatures() {
        return getActiveFeaturesFromMap(getModuleFeaturesMap());
    }
    
    private Set<String> getActiveFeaturesFromMap(Map<String, Boolean> theMap) {

        Set<String> activeFeatures = new TreeSet<String>();

        for (Entry<String, Boolean> feature : theMap.entrySet()) {
            if (feature.getValue().equals(Boolean.TRUE)) {
                activeFeatures.add(feature.getKey());
            }
        }

        return activeFeatures;
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.feature.WebcertFeatureService#getActiveFeatures()
     */
    @Override
    public Set<String> getActiveFeatures() {
        
        Set<String> activeFeatures = new TreeSet<String>();
        
        activeFeatures.addAll(getActiveWebcertFeatures());
        activeFeatures.addAll(getActiveModuleFeatures());
                
        return activeFeatures;
    }
    
    public Properties getFeatures() {
        return features;
    }

    public void setFeatures(Properties features) {
        this.features = features;
    }

    public Map<String, Boolean> getWebcertFeaturesMap() {
        return webcertFeaturesMap;
    }
    
    public Map<String, Boolean> getModuleFeaturesMap() {
        return moduleFeaturesMap;
    }

}
