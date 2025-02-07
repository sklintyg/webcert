/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;

/**
 * Encapsulates relations for a given certificate, split into zero to one parent relations (I originate from) and 0..n
 * child relations (I am the parent of).
 * Created by eriklupander on 2017-05-15.
 */
public class Relations {

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

    public static class FrontendRelations {

        WebcertCertificateRelation replacedByUtkast;
        WebcertCertificateRelation replacedByIntyg;
        WebcertCertificateRelation complementedByUtkast;
        WebcertCertificateRelation complementedByIntyg;
        WebcertCertificateRelation utkastCopy;

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

        public WebcertCertificateRelation getUtkastCopy() {
            return utkastCopy;
        }

        public void setUtkastCopy(WebcertCertificateRelation utkastCopy) {
            this.utkastCopy = utkastCopy;
        }

        @Override
        public String toString() {
            final List<String> list = new ArrayList<>();
            if (this.complementedByIntyg != null) {
                list.add(complementedByIntyg.toString());
            }
            if (this.complementedByUtkast != null) {
                list.add(complementedByUtkast.toString());
            }
            if (this.replacedByIntyg != null) {
                list.add(replacedByIntyg.toString());
            }
            if (this.replacedByUtkast != null) {
                list.add(replacedByUtkast.toString());
            }
            if (this.utkastCopy != null) {
                list.add(utkastCopy.toString());
            }
            return !list.isEmpty() ? list.toString() : "Empty FrontendRelations";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FrontendRelations that = (FrontendRelations) o;
            return Objects.equals(replacedByUtkast, that.replacedByUtkast) && Objects.equals(replacedByIntyg,
                that.replacedByIntyg) && Objects.equals(complementedByUtkast, that.complementedByUtkast) && Objects.equals(
                complementedByIntyg, that.complementedByIntyg) && Objects.equals(utkastCopy, that.utkastCopy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replacedByUtkast, replacedByIntyg, complementedByUtkast, complementedByIntyg, utkastCopy);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Relations relations = (Relations) o;
        return Objects.equals(parent, relations.parent) && Objects.equals(latestChildRelations,
            relations.latestChildRelations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, latestChildRelations);
    }

    @Override
    public String toString() {
        return "Relations{"
            + "parent=" + parent
            + ", latestChildRelations=" + latestChildRelations
            + '}';
    }
}
