package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser implements Serializable {

    private String hsaId;
    private String namn;
    private boolean lakare;
    private String prescriptionCode;

    private Vardgivare vardgivare;

    private List<Vardenhet> vardenheter;

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public boolean isLakare() {
        return lakare;
    }

    public void setLakare(boolean lakare) {
        this.lakare = lakare;
    }

    public Vardgivare getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }
    
    @JsonIgnore
    public String getAsJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
    }

    public List<String> getVardEnheter() {
        List<String> list = new ArrayList<String>();
        for(Vardenhet enhet: vardenheter) {
            list.add(enhet.getId());
            for(Mottagning mottagning: enhet.getMottagningar()) {
                list.add(mottagning.getId());
            }
        }
        return list;
    }

    public String getPrescriptionCode() {
        return prescriptionCode;
    }

    public void setPrescriptionCode(String prescriptionCode) {
        this.prescriptionCode = prescriptionCode;
    }

    public List<Vardenhet> getVardenheter() {
        return vardenheter;
    }

    public void setVardenheter(List<Vardenhet> vardenheter) {
        this.vardenheter = vardenheter;
    }
}
