package se.inera.webcert.notifications.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.notifications.routes.RouteHeaders;

public class IntygsTypCheckerImpl implements IntygsTypChecker {

    private static final Logger LOG = LoggerFactory.getLogger(IntygsTypCheckerImpl.class);

    private List<String> allowedIntygsTypes = new ArrayList<>();

    @Override
    public boolean checkIntygsTyp(@Header(RouteHeaders.INTYGS_ID) String intygsId, @Header(RouteHeaders.INTYGS_TYP) String intygsTyp) {

        LOG.debug("Checking intyg '{}' with type '{}'", intygsId, intygsTyp);

        if (allowedIntygsTypes.contains(intygsTyp)) {
            return true;
        }

        LOG.debug("Intygstyp '{}' is not allowed for processing", intygsTyp);
        return false;
    }

    public List<String> getAllowedIntygsTypes() {
        return allowedIntygsTypes;
    }

    public void setAllowedIntygsTypes(List<String> allowedIntygsTypes) {
        this.allowedIntygsTypes = allowedIntygsTypes;
    }
}
