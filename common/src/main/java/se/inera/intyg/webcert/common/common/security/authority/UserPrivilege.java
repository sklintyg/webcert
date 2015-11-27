package se.inera.intyg.webcert.common.common.security.authority;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserPrivilege {

    PRIVILEGE_SIGNERA_INTYG ("Signera intyg"),
    PRIVILEGE_MAKULERA_INTYG ("Makulera intyg"),
    PRIVILEGE_KOPIERA_INTYG ("Kopiera intyg"),
    PRIVILEGE_VIDAREBEFORDRA_UTKAST ("Vidarebefordra utkast"),
    PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR ("Vidarebefordra frågasvar"),
    PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA ("Besvara fråga om komplettering"),
    PRIVILEGE_FILTRERA_PA_LAKARE ("Filtrera på annan läkare"),
    PRIVILEGE_ATKOMST_ANDRA_ENHETER ("Åtkomst andra vårdenheter"),
    PRIVILEGE_HANTERA_PERSONUPPGIFTER ("Hantera personuppgifter"),
    PRIVILEGE_HANTERA_MAILSVAR ("Hantera notifieringsmail om frågasvar"),
    PRIVILEGE_NAVIGERING ("Navigera i menyer, på logo, tillbakaknappar");

    private final String text;

    UserPrivilege(String text) {
        this.text = text;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name().equals(otherName);
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    public String text() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }
}
