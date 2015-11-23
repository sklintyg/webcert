package se.inera.intyg.webcert.persistence.utkast.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "SCHEDULERAT_JOBB")
public class ScheduleratJobb {

    @Id
    @Column(name = "JOBB_ID")
    private String id;

    @Version
    @Column(name = "VERSION")
    private long version;

    @Column(name = "BEARBETAS")
    private boolean bearbetas;

    public ScheduleratJobb() {
        super();
    }

    public ScheduleratJobb(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isBearbetas() {
        return bearbetas;
    }

    public void setBearbetas(boolean bearbetas) {
        this.bearbetas = bearbetas;
    }
}
