package se.inera.webcert.persistence.fragasvar.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author marced
 * 
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

    @Override
    public int hashCode() {
        return falt.hashCode() + text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Komplettering)) {
            return false;
        } else {
            Komplettering other = (Komplettering) obj;
            return falt.equals(other.falt) && text.equals(other.text);
        }
    }
}
