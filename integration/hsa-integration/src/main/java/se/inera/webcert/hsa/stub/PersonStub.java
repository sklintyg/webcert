package se.inera.webcert.hsa.stub;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannesc
 */
public class PersonStub {

    private String hsaId;
    private String name;

    private List<String> medarbetaruppdrag = new ArrayList<>();

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMedarbetaruppdrag() {
        return medarbetaruppdrag;
    }

    public void setMedarbetaruppdrag(List<String> medarbetaruppdrag) {
        this.medarbetaruppdrag = medarbetaruppdrag;
    }

}
