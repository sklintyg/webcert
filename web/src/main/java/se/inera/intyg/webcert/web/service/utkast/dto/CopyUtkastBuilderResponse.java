package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public class CopyUtkastBuilderResponse {

    private Utkast utkastCopy;

    private String orginalEnhetsId;

    private String orginalEnhetsNamn;

    private String orginalVardgivarId;

    private String orginalVardgivarNamn;

    public Utkast getUtkastCopy() {
        return utkastCopy;
    }

    public void setUtkastCopy(Utkast utkastCopy) {
        this.utkastCopy = utkastCopy;
    }

    public String getOrginalEnhetsId() {
        return orginalEnhetsId;
    }

    public void setOrginalEnhetsId(String orginalEnhetsId) {
        this.orginalEnhetsId = orginalEnhetsId;
    }

    public String getOrginalEnhetsNamn() {
        return orginalEnhetsNamn;
    }

    public void setOrginalEnhetsNamn(String orginalEnhetsNamn) {
        this.orginalEnhetsNamn = orginalEnhetsNamn;
    }

    public String getOrginalVardgivarId() {
        return orginalVardgivarId;
    }

    public void setOrginalVardgivarId(String orginalVardgivarId) {
        this.orginalVardgivarId = orginalVardgivarId;
    }

    public String getOrginalVardgivarNamn() {
        return orginalVardgivarNamn;
    }

    public void setOrginalVardgivarNamn(String orginalVardgivarNamn) {
        this.orginalVardgivarNamn = orginalVardgivarNamn;
    }
}
