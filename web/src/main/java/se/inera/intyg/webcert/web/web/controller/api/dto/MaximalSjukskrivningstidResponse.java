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
    private int totalTidigareSjukskrivningsTid;
    private int totalSjukskrivningsTidInklusiveForeslagen;
    private Integer maximaltRekommenderadSjukskrivningstid;

    public MaximalSjukskrivningstidResponse() {
    }

    private MaximalSjukskrivningstidResponse(
            final int foreslagenSjukskrivningstid,
            final boolean overskriderRekommenderadSjukskrivningstid,
            final int totalTidigareSjukskrivningsTid,
            final int totalSjukskrivningsTidInklusiveForeslagen,
            final Integer maximalRekommenderadSjukskrivningstid) {
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
        this.overskriderRekommenderadSjukskrivningstid = overskriderRekommenderadSjukskrivningstid;
        this.totalTidigareSjukskrivningsTid = totalTidigareSjukskrivningsTid;
        this.totalSjukskrivningsTidInklusiveForeslagen = totalSjukskrivningsTidInklusiveForeslagen;
        this.maximaltRekommenderadSjukskrivningstid = maximalRekommenderadSjukskrivningstid;
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

    public int getTotalTidigareSjukskrivningsTid() {
        return totalTidigareSjukskrivningsTid;
    }

    public void setTotalTidigareSjukskrivningsTid(final int totalTidigareSjukskrivningsTid) {
        this.totalTidigareSjukskrivningsTid = totalTidigareSjukskrivningsTid;
    }

    public int getTotalSjukskrivningsTidInklusiveForeslagen() {
        return totalSjukskrivningsTidInklusiveForeslagen;
    }

    public void setTotalSjukskrivningsTidInklusiveForeslagen(final int totalSjukskrivningsTidInklusiveForeslagen) {
        this.totalSjukskrivningsTidInklusiveForeslagen = totalSjukskrivningsTidInklusiveForeslagen;
    }

    public Integer getMaximaltRekommenderadSjukskrivningstid() {
        return maximaltRekommenderadSjukskrivningstid;
    }

    public void setMaximaltRekommenderadSjukskrivningstid(final Integer maximaltRekommenderadSjukskrivningstid) {
        this.maximaltRekommenderadSjukskrivningstid = maximaltRekommenderadSjukskrivningstid;
    }

    public static MaximalSjukskrivningstidResponse fromFmbRekommendation(
            final int totalTidigareSjukskrivningsTid,
            final int foreslagenSjukskrivningstid,
            final int maximaltRekommenderadSjukskrivningstid) {

        final int totalSjukskrivningsTidInklusiveForeslagen = totalTidigareSjukskrivningsTid + foreslagenSjukskrivningstid;
        final boolean overskriden = totalSjukskrivningsTidInklusiveForeslagen > maximaltRekommenderadSjukskrivningstid;

        return new MaximalSjukskrivningstidResponse(
                foreslagenSjukskrivningstid,
                overskriden,
                totalTidigareSjukskrivningsTid,
                totalSjukskrivningsTidInklusiveForeslagen,
                maximaltRekommenderadSjukskrivningstid);
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
                null);
    }
}
