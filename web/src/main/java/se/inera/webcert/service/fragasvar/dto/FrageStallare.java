package se.inera.webcert.service.fragasvar.dto;

public enum FrageStallare {

    FORSAKRINGSKASSAN("FK"),
    WEBCERT("WC");

    private String kod;

    FrageStallare(String kod) {
        this.kod = kod;
    }

    public boolean equals(String kodValue) {
        return this.kod.equalsIgnoreCase(kodValue);
    }

    public String getKod() {
        return this.kod;
    }

    public static FrageStallare getByKod(String kodVal) {
        for (FrageStallare f : values()) {
            if (f.getKod().equals(kodVal)) {
                return f;
            }
        }
        return null;
    }
}
