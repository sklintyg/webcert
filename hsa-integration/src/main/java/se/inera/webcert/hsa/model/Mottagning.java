package se.inera.webcert.hsa.model;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Mottagning {

    private String id;
    private String namn;

    private LocalDateTime start;
    private LocalDateTime end;

    public Mottagning() {

    }

    public Mottagning(String id, String namn) {
        this.id = id;
        this.namn = namn;
    }

    public Mottagning(String id, String namn, LocalDateTime start, LocalDateTime end) {
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
