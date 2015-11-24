package se.inera.intyg.webcert.common.common.hsa;

public enum HSABefattning {

    // Överläkare
    OVERLAKARE("201010", "Överläkare"),

    // Distriktsläkare/Specialist allmänmedicin
    DISTRIKTSlAKARE("201011", "Distriktsläkare/Specialist allmänmedicin"),

    // Skolläkare
    SKOLLAKARE("201012", "Skolläkare"),

    // Företagsläkare
    FORETAGSLAKARE("201013", "Företagsläkare"),

    // Specialistläkare
    SPECIALISTLAKARE("202010", "Specialistläkare"),

    // Legitimerad läkare under specialiseringstjänstgöring (STläkare)
    LAKARE_LEG_ST("203010", "Läkare legitimerad, specialiseringstjänstgöring"),

    // Legitimerad läkare under till exempel vikariat
    LAKARE_LEG_ANNAN("203090", "Läkare legitimerad, annan"),

    // Ej legitimerad läkare under allmäntjänstgöring (AT-läkare)
    LAKARE_EJ_LEG_AT("204010", "Läkare ej legitimerad, allmäntjänstgöring"),

    // Ej legitimerad läkare under till exempel vikariat eller provtjänstgöring
    LAKARE_EJ_LEG_ANNAN("204090", "Läkare ej legitimerad, annan");

    private String code;

    private String description;

    HSABefattning(String code, String desc) {
        this.code = code;
        this.description = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static HSABefattning getByCode(String code) {
        for (HSABefattning bef : values()) {
            if (bef.getCode().equals(code)) {
                return bef;
            }
        }

        return null;
    }
}
