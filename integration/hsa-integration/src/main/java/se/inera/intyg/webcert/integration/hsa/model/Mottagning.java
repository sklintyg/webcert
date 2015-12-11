package se.inera.intyg.webcert.integration.hsa.model;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Mottagning extends AbstractVardenhet {

    private static final long serialVersionUID = 6427228467181041893L;

    private LocalDateTime start;
    private LocalDateTime end;
    private String parentHsaId;

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

    public String getParentHsaId() {
        return parentHsaId;
    }

    public void setParentHsaId(String parentHsaId) {
        this.parentHsaId = parentHsaId;
    }

    @Override
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        ids.add(getId());
        return ids;
    }
}
