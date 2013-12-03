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
        return vardgivare;
    }

    public void setVardgivare(List<Vardgivare> vardgivare) {
        this.vardgivare = vardgivare;
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

    public List<String> getVardenheterIds() {
        List<String> list = new ArrayList<String>();
        for(Vardgivare v : vardgivare) {
            list.addAll(v.getHsaIds());
        }
        return list;
    }

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }
}
