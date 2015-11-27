package se.inera.intyg.webcert.web.service.intyg.dto;

import java.util.List;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;

public class IntygContentHolder {

    @JsonRawValue
    private final String contents;

    @JsonIgnore
    private final Utlatande utlatande;
    private final List<Status> statuses;
    private final boolean revoked;


    public IntygContentHolder(String contents, Utlatande utlatande, List<Status> statuses, boolean revoked) {
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

    public List<Status> getStatuses() {
        return statuses;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
