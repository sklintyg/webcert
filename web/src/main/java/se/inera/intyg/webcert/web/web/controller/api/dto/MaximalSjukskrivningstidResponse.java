/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

public final class MaximalSjukskrivningstidResponse {

    private int foreslagenSjukskrivningstid;
    private boolean overskriderRekommenderadSjukskrivningstid;
    private int totalTidigareSjukskrivningstid;
    private int totalSjukskrivningstidInklusiveForeslagen;
    private Integer maximaltRekommenderadSjukskrivningstid;
    private String aktuellIcd10Kod;

    public MaximalSjukskrivningstidResponse() {
    }

    private MaximalSjukskrivningstidResponse(
            final int foreslagenSjukskrivningstid,
            final boolean overskriderRekommenderadSjukskrivningstid,
            final int totalTidigareSjukskrivningstid,
            final int totalSjukskrivningstidInklusiveForeslagen,
            final Integer maximalRekommenderadSjukskrivningstid,
            final String aktuellIcd10Kod) {
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
        this.overskriderRekommenderadSjukskrivningstid = overskriderRekommenderadSjukskrivningstid;
        this.totalTidigareSjukskrivningstid = totalTidigareSjukskrivningstid;
        this.totalSjukskrivningstidInklusiveForeslagen = totalSjukskrivningstidInklusiveForeslagen;
        this.maximaltRekommenderadSjukskrivningstid = maximalRekommenderadSjukskrivningstid;
        this.aktuellIcd10Kod = aktuellIcd10Kod;
    }

    public int getForeslagenSjukskrivningstid() {
        return foreslagenSjukskrivningstid;
    }

    public void setForeslagenSjukskrivningstid(final int foreslagenSjukskrivningstid) {
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
    }

    public boolean isOverskriderRekommenderadSjukskrivningstid() {
        return overskriderRekommenderadSjukskrivningstid;
    }

    public void setOverskriderRekommenderadSjukskrivningstid(final boolean overskriderRekommenderadSjukskrivningstid) {
        this.overskriderRekommenderadSjukskrivningstid = overskriderRekommenderadSjukskrivningstid;
    }

    public int getTotalTidigareSjukskrivningstid() {
        return totalTidigareSjukskrivningstid;
    }

    public void setTotalTidigareSjukskrivningstid(final int totalTidigareSjukskrivningstid) {
        this.totalTidigareSjukskrivningstid = totalTidigareSjukskrivningstid;
    }

    public int getTotalSjukskrivningstidInklusiveForeslagen() {
        return totalSjukskrivningstidInklusiveForeslagen;
    }

    public void setTotalSjukskrivningstidInklusiveForeslagen(final int totalSjukskrivningstidInklusiveForeslagen) {
        this.totalSjukskrivningstidInklusiveForeslagen = totalSjukskrivningstidInklusiveForeslagen;
    }

    public Integer getMaximaltRekommenderadSjukskrivningstid() {
        return maximaltRekommenderadSjukskrivningstid;
    }

    public void setMaximaltRekommenderadSjukskrivningstid(final Integer maximaltRekommenderadSjukskrivningstid) {
        this.maximaltRekommenderadSjukskrivningstid = maximaltRekommenderadSjukskrivningstid;
    }

    public String getAktuellIcd10Kod() {
        return aktuellIcd10Kod;
    }

    public void setAktuellIcd10Kod(final String aktuellIcd10Kod) {
        this.aktuellIcd10Kod = aktuellIcd10Kod;
    }

    public static MaximalSjukskrivningstidResponse fromFmbRekommendation(
            final int totalTidigareSjukskrivningsTid,
            final int foreslagenSjukskrivningstid,
            final int maximaltRekommenderadSjukskrivningstid,
            final String aktuellIcd10Kod) {

        final int totalSjukskrivningsTidInklusiveForeslagen = totalTidigareSjukskrivningsTid + foreslagenSjukskrivningstid;
        final boolean overskriden = totalSjukskrivningsTidInklusiveForeslagen > maximaltRekommenderadSjukskrivningstid;

        return new MaximalSjukskrivningstidResponse(
                foreslagenSjukskrivningstid,
                overskriden,
                totalTidigareSjukskrivningsTid,
                totalSjukskrivningsTidInklusiveForeslagen,
                maximaltRekommenderadSjukskrivningstid,
                aktuellIcd10Kod);
    }

    public static MaximalSjukskrivningstidResponse ingenFmbRekommendation(
            final int totalTidigareSjukskrivningsTid,
            final int foreslagenSjukskrivningstid) {

        final int totalSjukskrivningsTidInklusiveForeslagen = totalTidigareSjukskrivningsTid + foreslagenSjukskrivningstid;

        return new MaximalSjukskrivningstidResponse(
                foreslagenSjukskrivningstid,
                false,
                totalTidigareSjukskrivningsTid,
                totalSjukskrivningsTidInklusiveForeslagen,
                null,
                null);
    }
}
