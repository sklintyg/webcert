package se.inera.webcert.service.feature;

import java.util.Collections;
import java.util.Enumeration;
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
import org.springframework.util.Assert;

import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.webcert.modules.IntygModuleRegistry;

/**
 * Service that keeps track of active features of Webcert and installed modules. 
 * 
 * @author npet
 *
 */
@Service
public class WebcertFeatureServiceImpl implements WebcertFeatureService {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertFeatureService.class);

    private static final String COMMA_SEP = ", ";
    private static final String DOT_SEP = ".";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    @Qualifier("webcertFeatures")
    private Properties features;

    private Map<String, Boolean> featuresMap = new HashMap<String, Boolean>();

    /**
     * Performs initialization of the featuresMap.
     */
    @PostConstruct
    public void initFeaturesMap() {
        initWebcertFeatures(featuresMap);
        initModuleFeatures(featuresMap);
        processWebcertAndModuleFeatureProperties(features, featuresMap);

        LOG.info("Active Webcert features is: {}", StringUtils.join(getActiveFeatures(), COMMA_SEP));
    }

    /**
     * Inits the featuresMap with all Webcert features set to FALSE.
     * 
     * @param featuresMap
     */
    public void initWebcertFeatures(Map<String, Boolean> featuresMap) {
        
        Assert.notNull(featuresMap);
        
        for (Features feature : Features.values()) {
            featuresMap.put(feature.getName(), Boolean.FALSE);
        }
    }

    /**
     * Inits the featuresMap with module features. The features are collected from the modules
     * using the ModuleEntryPoint of the module. The names of the module feature is then qualified
     * using the id of the module.
     * 
     * @param featuresMap
     */
    public void initModuleFeatures(Map<String, Boolean> featuresMap) {
    
        Assert.notNull(featuresMap);
        
        String moduleId;
        Map<String, Boolean> moduleMap;
    
        for (ModuleEntryPoint mep : moduleRegistry.getModuleEntryPoints()) {
    
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
                featuresMap.put(key, moduleFeatureState);
            }
        }
    
    }

    /**
     * Reads the supplied properties and updates the state of the feature in the featuresMap. 
     * 
     * @param featureProps
     * @param featuresMap
     */
    @SuppressWarnings("rawtypes")
    public void processWebcertAndModuleFeatureProperties(Properties featureProps, Map<String, Boolean> featuresMap) {
        
        Assert.notNull(featureProps);
        Assert.notEmpty(featuresMap);
        
        Enumeration e = featureProps.propertyNames();

        while (e.hasMoreElements()) {
            String featureKey = (String) e.nextElement();

            Boolean featureState = Boolean.parseBoolean(featureProps.getProperty(featureKey));

            if (featureState != null) {
                featuresMap.put(featureKey, featureState);
                continue;
            }

        }
    }

    /* (non-Javadoc)
     * @see se.inera.webcert.service.feature.WebcertFeatureService#isFeatureActive(se.inera.webcert.service.feature.Features)
     */
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
        Boolean featureState = featuresMap.get(featureName);
        return (featureState != null) ? featureState.booleanValue() : false;
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.feature.WebcertFeatureService#isModuleFeatureActive(se.inera.certificate.modules.support.feature.ModuleFeature, java.lang.String)
     */
    public boolean isModuleFeatureActive(ModuleFeature moduleFeature, String moduleName) {
        return isModuleFeatureActive(moduleFeature.getName(), moduleName);
    }
    
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.feature.WebcertFeatureService#isModuleFeatureActive(java.lang.String, java.lang.String)
     */
    public boolean isModuleFeatureActive(String moduleFeatureName, String moduleName) {
        if (isFeatureActive(moduleFeatureName)) {
            String key = StringUtils.join(new String[] { moduleFeatureName, moduleName }, DOT_SEP);
            Boolean moduleFeatureState = featuresMap.get(key);
            return (moduleFeatureState != null) ? moduleFeatureState.booleanValue() : false;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.feature.WebcertFeatureService#getActiveFeatures()
     */
    @Override
    public Set<String> getActiveFeatures() {
        Set<String> activeFeatures = new TreeSet<String>();

        for (Entry<String, Boolean> feature : featuresMap.entrySet()) {
            if (feature.getValue().equals(Boolean.TRUE)) {
                activeFeatures.add(feature.getKey());
            }
        }

        return activeFeatures;
    }

    public Properties getFeatures() {
        return features;
    }

    public void setFeatures(Properties features) {
        this.features = features;
    }

    public Map<String, Boolean> getFeaturesMap() {
        return featuresMap;
    }

}
