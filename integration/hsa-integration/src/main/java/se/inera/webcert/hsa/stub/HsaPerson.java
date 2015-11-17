package se.inera.webcert.hsa.stub;

import java.util.ArrayList;
import java.util.List;

public class HsaPerson {

    private String hsaId;

    private String forNamn;

    private String efterNamn;

    private List<HsaSpecialicering> specialiseringar = new ArrayList<HsaSpecialicering>();

    private List<String> enhetIds = new ArrayList<String>();

    private String titel;

    private List<String> legitimeradeYrkesgrupper = new ArrayList<String>();

    private String befattningsKod;

    private String forskrivarKod;


    // ~ Constructors
    // ~ =====================================================================================

    public HsaPerson() {
        super();
    }

    public HsaPerson(String hsaId, String forNamn, String efterNamn) {
        super();
        this.hsaId = hsaId;
        this.forNamn = forNamn;
        this.efterNamn = efterNamn;
    }


    // ~ Getters and setters
    // ~ =====================================================================================

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getForNamn() {
        return forNamn;
    }

    public void setForNamn(String forNamn) {
        this.forNamn = forNamn;
    }

    public String getEfterNamn() {
        return efterNamn;
    }

    public void setEfterNamn(String efterNamn) {
        this.efterNamn = efterNamn;
    }

    public List<HsaSpecialicering> getSpecialiseringar() {
        return specialiseringar;
    }

    public void setSpecialiseringar(List<HsaSpecialicering> specialiseringar) {
        this.specialiseringar = specialiseringar;
    }

    public List<String> getEnhetIds() {
        return enhetIds;
    }

    public void setEnhetIds(List<String> enhetIds) {
        this.enhetIds = enhetIds;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public List<String> getLegitimeradeYrkesgrupper() {
        return legitimeradeYrkesgrupper;
    }

    public void setLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        this.legitimeradeYrkesgrupper = legitimeradeYrkesgrupper;
    }

    public String getBefattningsKod() {
        return befattningsKod;
    }

    public void setBefattningsKod(String befattningsKod) {
        this.befattningsKod = befattningsKod;
    }

    public String getForskrivarKod() {
        return forskrivarKod;
    }

    public void setForskrivarKod(String forskrivarKod) {
        this.forskrivarKod = forskrivarKod;
    }

}
