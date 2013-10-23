package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Vardgivare implements Serializable {

    private String id;
    private String namn;

    private List<Vardenhet> vardenheter = new ArrayList<>();

    public Vardgivare() {
    }

    public Vardgivare(String id, String namn) {
        this.id = id;
        this.namn = namn;
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

    public List<Vardenhet> getVardenheter() {
        return vardenheter;
    }
    
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        for (Vardenhet vardenhet : vardenheter) {
            ids.addAll(vardenhet.getHsaIds());
        }
        return ids;
    }

    public void setVardenheter(List<Vardenhet> vardenheter) {
        this.vardenheter = vardenheter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vardgivare that = (Vardgivare) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
