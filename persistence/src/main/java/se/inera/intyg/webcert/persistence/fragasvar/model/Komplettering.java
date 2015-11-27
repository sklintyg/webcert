package se.inera.intyg.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author marced
 */
@Embeddable
public class Komplettering {

    @Column(name = "FALT")
    private String falt;

    @Column(name = "TEXT")
    private String text;

    public String getFalt() {
        return falt;
    }

    public void setFalt(String falt) {
        this.falt = falt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
