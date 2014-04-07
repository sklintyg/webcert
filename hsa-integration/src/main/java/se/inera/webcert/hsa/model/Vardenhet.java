package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Vardenhet extends AbstractVardenhet implements Serializable {
    
    private String mail;

    private LocalDateTime start;
    private LocalDateTime end;

    private List<Mottagning> mottagningar;

    public Vardenhet() {
    }

    public Vardenhet(String id, String namn) {
        super(id, namn);
    }

    public Vardenhet(String id, String namn, LocalDateTime start, LocalDateTime end) {
        super(id, namn);
        this.start = start;
        this.end = end;
    }
    
    public List<Mottagning> getMottagningar() {
        if (mottagningar == null) {
            mottagningar = new ArrayList<>();
        }
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
        ids.add(getId());
        for (Mottagning mottagning : getMottagningar()) {
            ids.add(mottagning.getId());
        }
        return ids;
    }
    
    public SelectableVardenhet findSelectableVardenhet(String id) {
        
        if (id.equals(getId())) {
            return this;
        }
        
        for (Mottagning m : getMottagningar()) {
            if (id.equals(m.getId())) {
                return m;
            }
        }
        
        return null;
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
