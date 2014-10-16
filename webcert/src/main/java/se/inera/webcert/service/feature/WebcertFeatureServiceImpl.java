package se.inera.webcert.service.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class WebcertFeatureServiceImpl implements WebcertFeatureService {
    
    private static final Logger LOG = LoggerFactory.getLogger(WebcertFeatureService.class);
    
    @Autowired
    @Qualifier("webcertFeatures")
    private Properties features;

    private Map<String, Boolean> featuresMap = new HashMap<String, Boolean>();

    @PostConstruct
    public void initFeaturesMap() {        
        for (Features feature : Features.values()) {
            
            Boolean featureState = Boolean.parseBoolean(features.getProperty(feature.getName()));

            if (featureState == null) {
                featuresMap.put(feature.getName(), Boolean.FALSE);
                continue;
            }

            featuresMap.put(feature.getName(), featureState);
        }
        
        List<String> activeFeatures = getActiveFeatures();
        LOG.info("Active Webcert features is: {}", StringUtils.join(activeFeatures, ", "));
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.feature.WebcertFeatureService#isFeatureActive(java.lang.String)
     */
    @Override
    public boolean isFeatureActive(String featureName) {
        Boolean featureState = featuresMap.get(featureName);
        return (featureState != null) ? featureState.booleanValue() : false;
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.feature.WebcertFeatureService#getActiveFeatures()
     */
    @Override
    public List<String> getActiveFeatures() {
        
        List<String> activeFeatures = new ArrayList<String>();
        
        for (Entry<String,Boolean> feature : featuresMap.entrySet()) {
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
