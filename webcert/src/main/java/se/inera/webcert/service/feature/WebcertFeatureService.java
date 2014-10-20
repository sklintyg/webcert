package se.inera.webcert.service.feature;

import java.util.Set;

public interface WebcertFeatureService {

    public abstract boolean isFeatureActive(String featureName);

    public abstract boolean isFeatureActive(Features feature);
    
    public abstract Set<String> getActiveFeatures();

}
