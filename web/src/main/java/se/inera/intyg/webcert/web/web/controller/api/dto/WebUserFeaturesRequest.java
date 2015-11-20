package se.inera.intyg.webcert.web.web.controller.api.dto;

/**
 * Created by stephenwhite on 15/09/15.
 */
public class WebUserFeaturesRequest {

    private boolean jsMinified;
    private boolean jsLoggning;

    public boolean isJsLoggning() {
        return jsLoggning;
    }

    public void setJsLoggning(boolean jsLoggning) {
        this.jsLoggning = jsLoggning;
    }

    public boolean isJsMinified() {
        return jsMinified;
    }

    public void setJsMinified(boolean jsMinified) {
        this.jsMinified = jsMinified;
    }
}
