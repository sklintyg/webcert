package se.inera.intyg.webcert.integration.fmb.services;

public class FmbVersionStatus {

    private boolean fmbIsUpToDate;
    private boolean diagnosInfoIsUpToDate;

    public FmbVersionStatus(boolean fmbIsUpToDate, boolean diagnosInfoIsUpToDate) {
        this.fmbIsUpToDate = fmbIsUpToDate;
        this.diagnosInfoIsUpToDate = diagnosInfoIsUpToDate;
    }

    public boolean isFmbIsUpToDate() {
        return fmbIsUpToDate;
    }

    public boolean isDiagnosInfoIsUpToDate() {
        return diagnosInfoIsUpToDate;
    }

}
