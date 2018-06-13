package se.inera.intyg.webcert.web.service.underskrift.grp.dto;

import se.inera.intyg.infra.xmldsig.model.IntygSignature;

public class IntygGRPSignature implements IntygSignature {

    private String intygJson;
    private String intygHash;

    public IntygGRPSignature(String intygJson, String intygHash) {
        this.intygJson = intygJson;
        this.intygHash = intygHash;
    }

    @Override
    public String getIntygJson() {
        return intygJson;
    }


    // For GRP, this is the JSON rep.
    @Override
    public String getCanonicalizedIntyg() {
        return intygJson;
    }

    @Override
    public String getSigningData() {
        return intygHash;
    }
}
