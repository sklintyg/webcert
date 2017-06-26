package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;

/**
 * Encapsulates relations for a given certificate, split into zero to one parent relations (I originate from) and 0..n
 * child relations (I am the parent of).
 *
 * Created by eriklupander on 2017-05-15.
 */
public class Relations {

    public static class FrontendRelations {
        WebcertCertificateRelation replacedByUtkast;
        WebcertCertificateRelation replacedByIntyg;
        WebcertCertificateRelation complementedByUtkast;
        WebcertCertificateRelation complementedByIntyg;

        public WebcertCertificateRelation getReplacedByUtkast() {
            return replacedByUtkast;
        }

        public void setReplacedByUtkast(WebcertCertificateRelation replacedByUtkast) {
            this.replacedByUtkast = replacedByUtkast;
        }

        public WebcertCertificateRelation getReplacedByIntyg() {
            return replacedByIntyg;
        }

        public void setReplacedByIntyg(WebcertCertificateRelation replacedByIntyg) {
            this.replacedByIntyg = replacedByIntyg;
        }

        public WebcertCertificateRelation getComplementedByUtkast() {
            return complementedByUtkast;
        }

        public void setComplementedByUtkast(WebcertCertificateRelation complementedByUtkast) {
            this.complementedByUtkast = complementedByUtkast;
        }

        public WebcertCertificateRelation getComplementedByIntyg() {
            return complementedByIntyg;
        }

        public void setComplementedByIntyg(WebcertCertificateRelation complementedByIntyg) {
            this.complementedByIntyg = complementedByIntyg;
        }
    }

    private WebcertCertificateRelation parent;
    private FrontendRelations latestChildRelations;

    public Relations() {
        this.latestChildRelations = new FrontendRelations();
    }

    public WebcertCertificateRelation getParent() {
        return parent;
    }

    public void setParent(WebcertCertificateRelation parent) {
        this.parent = parent;
    }

    public FrontendRelations getLatestChildRelations() {
        return latestChildRelations;
    }

    public void setLatestChildRelations(FrontendRelations latestChildRelations) {
        this.latestChildRelations = latestChildRelations;
    }
}
