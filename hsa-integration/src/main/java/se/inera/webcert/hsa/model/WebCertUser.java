package se.inera.webcert.hsa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import se.inera.certificate.integration.json.CustomObjectMapper;

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
    
    private SelectableVardenhet valdVardenhet;

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

    @JsonIgnore
    public String getAsJson() {
        try {
            return new CustomObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
    }
    
    @JsonIgnore
    public List<String> getVardenheterIds() {
        SelectableVardenhet selected = getValdVardenhet();
        
        if (selected == null) {
            return new ArrayList<>();
        }
        
        return selected.getHsaIds();
    }
    
    public SelectableVardenhet findSelectableVardenhet(String id) {
        
        if (id == null) {
            return null;
        }
                
        SelectableVardenhet ve = null;
        
        for (Vardgivare vg : getVardgivare()) {
            ve = vg.findVardenhet(id);
            if (ve != null) {
                return ve;
            }
        }
                
        return null;
    }
    
    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }
}
