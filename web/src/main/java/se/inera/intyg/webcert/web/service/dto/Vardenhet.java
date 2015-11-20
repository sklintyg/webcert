package se.inera.intyg.webcert.web.service.dto;

public class Vardenhet {

    private String hsaId;

    private String namn;

    private String postadress;

    private String postnummer;

    private String postort;

    private String telefonnummer;

    private String epost;

    private Vardgivare vardgivare;

    private String arbetsplatskod;

    public Vardenhet() {

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

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public Vardgivare getVardgivare() {
        return vardgivare;
    }

    public void setVardgivare(Vardgivare vardgivare) {
        this.vardgivare = vardgivare;
    }

    public String getArbetsplatskod() {
        return arbetsplatskod;
    }

    public void setArbetsplatskod(String arbetsplatskod) {
        this.arbetsplatskod = arbetsplatskod;
    }

    public static Vardenhet create(se.inera.webcert.hsa.model.Vardenhet hsaVardenhet) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setHsaId(hsaVardenhet.getId());
        vardenhet.setNamn(hsaVardenhet.getNamn());
        vardenhet.setArbetsplatskod(hsaVardenhet.getArbetsplatskod());
        vardenhet.setPostadress(hsaVardenhet.getPostadress());
        vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
        vardenhet.setPostort(hsaVardenhet.getPostort());
        vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());
        vardenhet.setEpost(hsaVardenhet.getEpost());
        return vardenhet;
    }

    public static Vardenhet create(se.inera.webcert.hsa.model.Vardenhet hsaVardenhet, se.inera.webcert.hsa.model.Vardgivare hsaVardgivare) {
        Vardgivare vardgivare = Vardgivare.create(hsaVardgivare);
        Vardenhet vardenhet = create(hsaVardenhet);
        vardenhet.setVardgivare(vardgivare);
        return vardenhet;
    }
}
