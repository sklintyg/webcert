package se.inera.intyg.webcert.web.service.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntrospectionResponse {

    private String scope;
    private boolean active;
    private long exp;

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "client_id")
    private String clientId;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
