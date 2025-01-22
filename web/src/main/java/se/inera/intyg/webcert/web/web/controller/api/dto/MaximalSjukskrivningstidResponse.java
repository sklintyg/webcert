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

public final class MaximalSjukskrivningstidResponse {

    private boolean overskriderRekommenderadSjukskrivningstid;
    private int totalSjukskrivningstid;
    private Integer maximaltRekommenderadSjukskrivningstid;
    private String aktuellIcd10Kod;
    private String maximaltRekommenderadSjukskrivningstidSource;

    public MaximalSjukskrivningstidResponse() {
    }

    private MaximalSjukskrivningstidResponse(
        final boolean overskriderRekommenderadSjukskrivningstid,
        final int totalSjukskrivningstid,
        final Integer maximalRekommenderadSjukskrivningstid,
        final String aktuellIcd10Kod,
        final String maximaltRekommenderadSjukskrivningstidSource) {
        this.overskriderRekommenderadSjukskrivningstid = overskriderRekommenderadSjukskrivningstid;
        this.totalSjukskrivningstid = totalSjukskrivningstid;
        this.maximaltRekommenderadSjukskrivningstid = maximalRekommenderadSjukskrivningstid;
        this.aktuellIcd10Kod = aktuellIcd10Kod;
        this.maximaltRekommenderadSjukskrivningstidSource = maximaltRekommenderadSjukskrivningstidSource;
    }

    public boolean isOverskriderRekommenderadSjukskrivningstid() {
        return overskriderRekommenderadSjukskrivningstid;
    }

    public void setOverskriderRekommenderadSjukskrivningstid(final boolean overskriderRekommenderadSjukskrivningstid) {
        this.overskriderRekommenderadSjukskrivningstid = overskriderRekommenderadSjukskrivningstid;
    }

    public int getTotalSjukskrivningstid() {
        return totalSjukskrivningstid;
    }

    public void setTotalSjukskrivningstid(final int totalSjukskrivningstid) {
        this.totalSjukskrivningstid = totalSjukskrivningstid;
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
        final int totalSjukskrivningstid,
        final int maximaltRekommenderadSjukskrivningstid,
        final String aktuellIcd10Kod,
        final String maximaltRekommenderadSjukskrivningstidSource) {

        final boolean overskriden = totalSjukskrivningstid > maximaltRekommenderadSjukskrivningstid;

        return new MaximalSjukskrivningstidResponse(
            overskriden,
            totalSjukskrivningstid,
            maximaltRekommenderadSjukskrivningstid,
            aktuellIcd10Kod,
            maximaltRekommenderadSjukskrivningstidSource);
    }

    public static MaximalSjukskrivningstidResponse ingenFmbRekommendation(
        final int totalSjukskrivningstid) {

        return new MaximalSjukskrivningstidResponse(
            false,
            totalSjukskrivningstid,
            null,
            null,
            null);
    }

    public String getMaximaltRekommenderadSjukskrivningstidSource() {
        return maximaltRekommenderadSjukskrivningstidSource;
    }

    public void setMaximaltRekommenderadSjukskrivningstidSource(String maximaltRekommenderadSjukskrivningstidSource) {
        this.maximaltRekommenderadSjukskrivningstidSource = maximaltRekommenderadSjukskrivningstidSource;
    }
}
