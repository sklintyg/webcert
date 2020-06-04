package se.inera.intyg.webcert.web.service.underskrift.dss;

public class DssSignRequestDTO {

    private String transactionId;
    private String signRequest;
    private String actionUrl;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSignRequest() {
        return signRequest;
    }

    public void setSignRequest(String signRequest) {
        this.signRequest = signRequest;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
