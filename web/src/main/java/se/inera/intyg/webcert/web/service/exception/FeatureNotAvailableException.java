package se.inera.intyg.webcert.web.service.exception;

public class FeatureNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 7688576313893632920L;

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
