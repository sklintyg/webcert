package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.integration.json.CustomObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser implements Serializable {

    private String hsaId;
    private String namn;
    private boolean lakare;
    private String forskrivarkod;
    private String authenticationScheme;

    private List<Vardgivare> vardgivare;
    
    private List<Specialisering> specialiseringar;
    
    private SelectableVardenhet valdVardenhet;
    
    private SelectableVardenhet valdVardgivare;

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

    public List<Vardgivare> getVardgivare() {
        
        if (vardgivare == null) {
            vardgivare = new ArrayList<Vardgivare>();
        }
        
        return vardgivare;
    }

    public void setVardgivare(List<Vardgivare> vardgivare) {
        this.vardgivare = vardgivare;
    }

    public List<Specialisering> getSpecialiseringar() {
        return specialiseringar;
    }

    public void setSpecialiseringar(List<Specialisering> specialiseringar) {
        this.specialiseringar = specialiseringar;
    }

    public SelectableVardenhet getValdVardenhet() {
        return valdVardenhet;
    }

    public void setValdVardenhet(SelectableVardenhet valdVardenhet) {
        this.valdVardenhet = valdVardenhet;
    }   

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }

    public SelectableVardenhet getValdVardgivare() {
        return valdVardgivare;
    }
    
    public void setValdVardgivare(SelectableVardenhet valdVardgivare) {
        this.valdVardgivare = valdVardgivare;
    }
    
    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    @JsonIgnore
    public String getAsJson() {
        try {
            return new CustomObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
    }
    
    @JsonIgnore
    public List<String> getIdsOfSelectedVardenhet() {
        
        SelectableVardenhet selected = getValdVardenhet();
        
        if (selected == null) {
            return new ArrayList<>();
        }
        
        return selected.getHsaIds();
    }
    
    @JsonIgnore
    public List<String> getIdsOfAllVardenheter() {
        
        List<Vardgivare> allVardgivare = getVardgivare();
                
        List<String> allIds = new ArrayList<String>();
        
        for (Vardgivare vardgivare : allVardgivare) {
            allIds.addAll(vardgivare.getHsaIds());
        }
        
        return allIds;    
    }
    
    public int getTotaltAntalVardenheter() {
        return getIdsOfAllVardenheter().size();
    }
    
    public boolean changeValdVardenhet(String vardenhetId) {
    
        if (vardenhetId == null) {
            return false;
        }
                
        SelectableVardenhet ve = null;
        
        for (Vardgivare vg : getVardgivare()) {
            ve = vg.findVardenhet(vardenhetId);
            if (ve != null) {
                setValdVardenhet(ve);
                setValdVardgivare(vg);
                return true;
            }
        }
                
        return false;
    }
}
