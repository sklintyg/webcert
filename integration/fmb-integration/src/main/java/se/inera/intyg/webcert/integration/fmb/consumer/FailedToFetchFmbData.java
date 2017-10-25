package se.inera.intyg.webcert.integration.fmb.consumer;

import java.io.IOException;

public class FailedToFetchFmbData extends Exception {

    private final String serviceIdentifier;

    public FailedToFetchFmbData(String serviceIdentifier, IOException cause) {
        super("Failed to call: " + serviceIdentifier, cause);
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

}
