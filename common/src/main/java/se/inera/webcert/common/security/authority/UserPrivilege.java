package se.inera.webcert.common.security.authority;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
public enum UserPrivilege {

    PRIVILEGE_SKRIVA_INTYG ("Skriva intyg"),
    PRIVILEGE_SIGNERA_INTYG ("Signera intyg"),
    PRIVILEGE_MAKULERA_INTYG ("Makulera intyg"),
    PRIVILEGE_KOPIERA_INTYG ("Kopiera intyg"),
    PRIVILEGE_VIDAREBEFORDRA_UTKAST ("Vidarebefordra utkast"),
    PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR ("Vidarebefordra frågasvar"),
    PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA ("Besvara fråga om komplettering");

    private final String text;

    private UserPrivilege(String text) {
        this.text = text;
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    public String toString() {
        return this.text;
    }

}
