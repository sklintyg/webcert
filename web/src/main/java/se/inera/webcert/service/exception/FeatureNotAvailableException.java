package se.inera.webcert.service.exception;

public class FeatureNotAvailableException extends RuntimeException {

    private String featureName;

    public FeatureNotAvailableException() {
        super();
    }

    public FeatureNotAvailableException(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
