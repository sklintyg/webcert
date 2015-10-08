package se.inera.webcert.hsa.stub;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannesc
 */
public class HsaUnitStub {

    private String vardgivarid;
    private String hsaId;
    private String email;
    private String name;

    private List<PersonStub> medarbetaruppdrag = new ArrayList<>();

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVardgivarid() {
        return vardgivarid;
    }

    public void setVardgivarid(String vardgivarid) {
        this.vardgivarid = vardgivarid;
    }

    public List<PersonStub> getMedarbetaruppdrag() {
        return medarbetaruppdrag;
    }

    public void setMedarbetaruppdrag(List<PersonStub> medarbetaruppdrag) {
        this.medarbetaruppdrag = medarbetaruppdrag;
    }

}
