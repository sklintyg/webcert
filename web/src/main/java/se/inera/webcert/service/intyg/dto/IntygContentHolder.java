package se.inera.webcert.service.intyg.dto;

import java.util.List;

import se.inera.certificate.model.common.internal.Utlatande;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class IntygContentHolder {

    @JsonRawValue
    private final String contents;

    private final Utlatande utlatande;
    private final List<IntygStatus> statuses;
    private final boolean revoked;


    public IntygContentHolder(String contents, Utlatande utlatande, List<IntygStatus> statuses, boolean revoked) {
        super();
        this.contents = contents;
        this.utlatande = utlatande;
        this.statuses = statuses;
        this.revoked = revoked;
    }

    public String getContents() {
        return contents;
    }

    public Utlatande getUtlatande() {
        return utlatande;
    }

    public List<IntygStatus> getStatuses() {
        return statuses;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
