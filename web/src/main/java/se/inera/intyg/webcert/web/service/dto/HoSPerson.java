package se.inera.intyg.webcert.web.service.dto;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.ArrayList;
import java.util.List;

public class HoSPerson {

    private String hsaId;

    private String namn;

    private String forskrivarkod;

    private String befattning;

    private List<String> specialiseringar;

    public HoSPerson() {

    }

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

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    public String getBefattning() {
        return befattning;
    }

    public void setBefattning(String befattning) {
        this.befattning = befattning;
    }

    public List<String> getSpecialiseringar() {
        if (specialiseringar == null) {
            specialiseringar = new ArrayList<>();
        }
        return specialiseringar;
    }

    public static HoSPerson create(WebCertUser user) {
        HoSPerson person = new HoSPerson();
        person.setHsaId(user.getHsaId());
        person.setNamn(user.getNamn());
        person.setForskrivarkod(user.getForskrivarkod());
        // TODO Sätt befattning
        return person;
    }
}
