package se.inera.webcert.hsa.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Vardenhet {

    private String id;
    private String namn;
    private String mail;

    private LocalDateTime start;
    private LocalDateTime end;

    private List<Mottagning> mottagningar = new ArrayList<>();

    public Vardenhet() {
    }

    public Vardenhet(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public Vardenhet(String id, String namn, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.namn = namn;
        this.start = start;
        this.end = end;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Mottagning> getMottagningar() {
        return mottagningar;
    }

    public void setMottagningar(List<Mottagning> mottagningar) {
        this.mottagningar = mottagningar;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        for (Mottagning mottagning : mottagningar) {
            ids.add(mottagning.getId());
        }
        return ids;
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
}
