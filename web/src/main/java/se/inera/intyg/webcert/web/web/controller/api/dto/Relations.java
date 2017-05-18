package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates relations for a given certificate, split into zero to one parent relations (I originate from) and 0..n
 * child relations (I am the parent of).
 *
 * Created by eriklupander on 2017-05-15.
 */
public class Relations {
    private WebcertCertificateRelation parent;
    private List<WebcertCertificateRelation> children;

    public WebcertCertificateRelation getParent() {
        return parent;
    }

    public void setParent(WebcertCertificateRelation parent) {
        this.parent = parent;
    }

    public List<WebcertCertificateRelation> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<WebcertCertificateRelation> children) {
        this.children = children;
    }
}
