package se.inera.webcert.service.feature;

import java.util.List;

public interface WebcertFeatureService {

    public abstract boolean isFeatureActive(String featureName);

    public abstract List<String> getActiveFeatures();

}
