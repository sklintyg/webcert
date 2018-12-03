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
