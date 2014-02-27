package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Mottagning extends AbstractVardenhet implements Serializable {

    private String mail;

    private LocalDateTime start;
    private LocalDateTime end;

    public Mottagning() {
        super();
    }

    public Mottagning(String id, String namn) {
        super(id, namn);
    }

    public Mottagning(String id, String namn, LocalDateTime start, LocalDateTime end) {
        super(id, namn);
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        ids.add(getId());
        return ids;
    }
}
